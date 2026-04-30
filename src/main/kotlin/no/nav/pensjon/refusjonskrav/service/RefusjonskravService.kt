package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.UnderArt
import no.nav.pensjon.refusjonskrav.domain.KredMap
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.*
import no.nav.pensjon.refusjonskrav.repository.KredMapRepository
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.AndreTrekk
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.OpprettAndreTrekkRequest
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.ArtTypeCode
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Melding
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.MeldingStatus
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.BESVART
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.IKKE_OVERFORT_PEN
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import org.slf4j.LoggerFactory.getLogger
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class RefusjonskravService(
    private val samClient: SamClient,
    private val tpClient: TpClient,
    private val osClient: OsClient,
    private val vedtakService: VedtakService,
    private val kredMapRepository: KredMapRepository
) {

    private val logger = getLogger(javaClass)

    private val lastDayOfNextMonth: LocalDate
        get() = LocalDate.now().plusMonths(2).withDayOfMonth(1).minusDays(1)


    fun behandleRefusjonskrav(refusjonskrav: Refusjonskrav, orgno: String?) {
        logger.info("Processing refusjonskrav for melding: ${refusjonskrav.samId}.")
        var melding = samClient.hentMelding(refusjonskrav.samId)
        if (orgno != null) tpClient.validateTpnr(melding.tpNr, orgno)

        logger.debug("Validating refusjonskrav.")
        if (refusjonskrav.validateFields(melding))
            samClient.opprettHendelse(melding.vedtak.person, melding.tpNr)

        melding = if (refusjonskrav.periodisertBelopListe.isNotEmpty())
            registrerSvar(
                melding,
                refusjonskrav.refusjonskrav
            )
        else {
            logger.info("Empty refusjonskrav, closing melding.")
            registrerSvar(melding, false)
            return
        }

        refusjonskrav.opprettAndreTrekk(melding)

        if (melding.vedtak.alleMeldingerBesvart)
            avsluttBehandling(melding.vedtak)
    }

    private fun Refusjonskrav.validateFields(melding: Melding) = when {
        pid != null && melding.pid != pid -> throw MismatchedPidException()
        tpNr != null && melding.tpNr != tpNr -> throw MismatchedTpnrException()
        melding.meldingStatus == MeldingStatus.BESVART || melding.vedtak.vedtakStatus == BESVART ->
            throw MeldingBesvartException()

        else -> {
            logger.debug("Validating refusjonstrekk.")
            periodisertBelopListe.isNotEmpty() && periodisertBelopListe.all { refusjonstrekk ->
                melding.validateRefusjonstrekk(refusjonstrekk)
            }
        }
    }

    private fun avsluttBehandling(vedtak: Vedtak) {
        try {
            vedtakService.lukkVedtak(vedtak)
            samClient.oppdaterVedtak(vedtak, BESVART)
        } catch (e: Exception) {
            logger.warn("Failed to close vedtak: ${vedtak.samVedtakId}.", e)
            if(vedtak.vedtakStatus != IKKE_OVERFORT_PEN)
                try {
                    samClient.oppdaterVedtak(vedtak, IKKE_OVERFORT_PEN)
                } catch (updateException: Exception) {
                    logger.error("Failed to update vedtak status to IKKE_OVERFORT_PEN: ${vedtak.samVedtakId}.", updateException)
                }
            throw CouldNotCloseVedtakException()
        }
    }

    private fun registrerSvar(melding: Melding, refusjonskrav: Boolean): Melding = samClient.updateMelding(
        melding,
        refusjonskrav,
        LocalDate.now(),
        MeldingStatus.BESVART
    )

    private fun Melding.validateRefusjonstrekk(refusjonstrekk: Refusjonstrekk) = when {
        refusjonstrekk.datoFom.toLocalDate().isBefore(vedtak.dateFom)
            -> throw TrekkStartBeforeVedtakException()
        refusjonstrekk.datoTom.isBefore(refusjonstrekk.datoFom) //Var refusjonTrekk.datoTom før vedtak.datoFom i SAM, ville aldri slå inn med mindre trekket var baklengs.
            -> throw TrekkEndBeforeStartException()
        vedtak.dateTom?.isBefore(refusjonstrekk.datoTom.toLocalDate()) == true
            -> throw TrekkEndAfterVedtakException()
        vedtak.dateTom == null && lastDayOfNextMonth.isBefore(refusjonstrekk.datoTom.toLocalDate())
            -> throw FutureTrekkOnRunningVedtakException()
        else -> true
    }

    private val Melding.prioritetFom
        get() = tpClient.getYtelser(pid, tpNr).run {
            if (onlyAndresYtelser) prioritetFom.plusYears(YEAR_ADD_FACTOR) else prioritetFom
        }
    private fun Refusjonskrav.createAndreTrekkRequest(melding: Melding): OpprettAndreTrekkRequest {
        val prioritetFom = melding.prioritetFom
        val tpKredMap = melding.kredCodes
        val tssEksternId = melding.tssEksternId
        val underArt = melding.vedtak.underArt
        return OpprettAndreTrekkRequest(
            periodisertBelopListe.map {
                AndreTrekk(
                    pid = melding.pid,
                    endringsKilde = tssEksternId,
                    prioritetFom = prioritetFom,
                    underArt = underArt,
                    kredMap = tpKredMap,
                    refusjonstrekk = it
                )
            }
        )
    }
    private fun Refusjonskrav.opprettAndreTrekk(melding: Melding) {
        osClient.opprettAndreTrekk(createAndreTrekkRequest(melding))
    }

    private val Set<Ytelse>.prioritetFom: LocalDate
        get() = maxByOrNull { it.innmeldtFom }?.innmeldtFom
            ?: throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Ingen ytelser funnet for tp-forhold.")
    private val Set<Ytelse>.onlyAndresYtelser
        get() = all { it.ytelseKode == "GJENLEVENDE" || it.ytelseKode == "BARN" }

    private val Melding.kredCodes: KredMap
        get() = kredMapRepository[tpNr]
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Kreditorinformasjon for tpnr $tpNr ikke funnet.")
    private val Melding.tssEksternId
        get() = tpClient.getTssEksternId(tpNr)

    private val Vedtak.underArt: UnderArt
        get() = when(art) {
            ArtTypeCode.FAM_PL, ArtTypeCode.OPPSATT_BTO_PEN, ArtTypeCode.SAERALDER
                -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Arttype $art støtter ikke refusjonskrav.")
            ArtTypeCode.UFOREP -> if (!dateFom.isBefore(LocalDate.of(2015, 1, 1))) UnderArt.UFOREUT
                else UnderArt.UFOREP
            else -> art.underArt!!
        }


    companion object {
        const val YEAR_ADD_FACTOR: Long = 200
    }
}
