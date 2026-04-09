package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.UnderArt
import no.nav.pensjon.refusjonskrav.domain.KredMap
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
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
internal class RefusjonskravService(
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
        if (refusjonskrav.validateFields(melding)) {
            samClient.opprettHendelse(melding.vedtak.person, melding.tpNr)

            melding = registrerSvar(melding, refusjonskrav.refusjonskrav)

            refusjonskrav.opprettAndreTrekk(melding)

            if (melding.vedtak.alleMeldingerBesvart)
                avsluttBehandling(melding.vedtak)
        }
    }

    private fun Refusjonskrav.validateFields(melding: Melding) = when {
        pid != null && melding.pid != pid -> throw ResponseStatusException(HttpStatus.CONFLICT, "Pid i kravet samsvarerer ikke med melding.")
        tpNr != null && melding.tpNr != tpNr -> throw ResponseStatusException(HttpStatus.CONFLICT, "Tpnr i kravet samsvarerer ikke med melding.")
        melding.meldingStatus == MeldingStatus.BESVART || melding.vedtak.vedtakStatus == BESVART ->
            throw ResponseStatusException(HttpStatus.CONFLICT, "Melding besvart eller tidsfrist utløpt.")

        periodisertBelopListe.isEmpty() -> {
            logger.info("Empty refusjonskrav, closing melding.")
            registrerSvar(melding, false)
            false
        }

        else -> {
            logger.debug("Validating refusjonstrekk.")
            periodisertBelopListe.all { refusjonstrekk ->
                melding.validateRefusjonstrekk(refusjonstrekk)
            }
        }
    }

    private fun avsluttBehandling(vedtak: Vedtak) {
        try {
            vedtakService.lukkVedtak(vedtak)
            samClient.oppdaterVedtak(vedtak, BESVART)
        } catch (_: Exception) {
            if(vedtak.vedtakStatus != IKKE_OVERFORT_PEN)
                samClient.oppdaterVedtak(vedtak, IKKE_OVERFORT_PEN)
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
            -> throw ResponseStatusException(HttpStatus.CONFLICT, "Refusjonstrekk starter før start av vedtak.")
        refusjonstrekk.datoTom.toLocalDate().isBefore(vedtak.dateFom)
            -> throw ResponseStatusException(HttpStatus.CONFLICT, "Refusjonstrekk slutter før start av vedtak.")
        vedtak.dateTom?.isBefore(refusjonstrekk.datoTom.toLocalDate()) == true
            -> throw ResponseStatusException(HttpStatus.CONFLICT, "Refusjonstrekk slutter etter avsluttning av vedtak.")
        vedtak.dateTom == null && lastDayOfNextMonth.isBefore(refusjonstrekk.datoTom.toLocalDate())
            -> throw ResponseStatusException(HttpStatus.CONFLICT, "Refusjonstrekk kan ikke være frem i tid for løpende vedtak.")
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
        get() = last().innmeldtFom
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
