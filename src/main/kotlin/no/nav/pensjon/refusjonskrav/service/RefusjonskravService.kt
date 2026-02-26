package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.domain.TPKredMap
import no.nav.pensjon.refusjonskrav.repository.TPKredMapRepository
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.AndreTrekk
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.OpprettAndreTrekkRequest
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.ArtType
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Melding
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.MeldingStatus
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.BESVART
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.VedtakStatus.IKKE_OVERFORT_PEN
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

//@Service
internal class RefusjonskravService(
    private val samClient: SamClient,
    private val tpClient: TpClient,
    private val osClient: OsClient,
    private val vedtakService: VedtakService,
    private val kredMapRepository: TPKredMapRepository
) {

    private val lastDayOfNextMonth: LocalDate
        get() = LocalDate.now().plusMonths(2).withDayOfMonth(1).minusDays(1)


    fun behandleRefusjonskrav(refusjonskrav: Refusjonskrav) {
        var melding = samClient.hentMelding(refusjonskrav.samId)

        when {
            melding.vedtak.person != refusjonskrav.pid -> TODO("Avvik hvis vedtak ikke samsvarer med kravet. Feil i request.")
            melding.tpNr != refusjonskrav.tpNr -> TODO("Avvik hvis melding ikke samsvarer med kravet. Feil i request.")
            melding.meldingStatus == MeldingStatus.BESVART || melding.vedtak.vedtakStatus == BESVART ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "Melding besvart eller tidsfrist utløpt.")

            refusjonskrav.periodisertBelopListe.isEmpty() -> {
                registrerSvar(melding, false)
                return
            }

            else -> {
                refusjonskrav.periodisertBelopListe.forEach { refusjonstrekk ->
                    melding.validateRefusjonstrekk(refusjonstrekk)
                }
            }
        }

        samClient.opprettHendelse(refusjonskrav.pid, refusjonskrav.tpNr)


        melding = registrerSvar(melding, refusjonskrav.refusjonskrav)

        refusjonskrav.opprettAndreTrekk(melding)

        if (melding.vedtak.alleMeldingerBesvart)
            avsluttBehandling(melding.vedtak)
    }

    /*TODO: Consider:
    try {
        vedtakService.lukkVedtak(vedtak)
        samClient.oppdaterVedtak(vedtak, BESVART)
    } catch (_: Exception) {
        if(vedtak.vedtakStatus != IKKE_OVERFORT_PEN)
            samClient.oppdaterVedtak(vedtak, IKKE_OVERFORT_PEN)
    }
     */
    private fun avsluttBehandling(vedtak: Vedtak) {
        if (vedtak.vedtakStatus != IKKE_OVERFORT_PEN)
            samClient.oppdaterVedtak(vedtak, IKKE_OVERFORT_PEN)

        if (vedtak.vedtakStatus == IKKE_OVERFORT_PEN)
            vedtakService.lukkVedtak(vedtak)

        samClient.oppdaterVedtak(vedtak, BESVART)
    }

    private fun registrerSvar(melding: Melding, refusjonskrav: Boolean): Melding = samClient.updateMelding(
        melding,
        refusjonskrav,
        LocalDate.now(),
        MeldingStatus.BESVART
    )

    private fun Melding.validateRefusjonstrekk(refusjonstrekk: Refusjonstrekk) {
        when {
            refusjonstrekk.datoFom.toLocalDate().isBefore(vedtak.dateFom) -> TODO("Kast exception.")
            refusjonstrekk.datoTom.toLocalDate().isBefore(vedtak.dateFom) -> TODO("Kast exception.")
            vedtak.dateTom?.isBefore(refusjonstrekk.datoTom.toLocalDate()) == true -> TODO("Kast exception.")
            vedtak.dateTom == null && lastDayOfNextMonth.isBefore(refusjonstrekk.datoTom.toLocalDate()) -> TODO("Kast exception.")
        }
    }

    private val Refusjonskrav.prioritetFom
        get() = tpClient.getYtelser(pid, tpNr).run {
            if (onlyAndresYtelser) prioritetFom.plusYears(YEAR_ADD_FACTOR) else prioritetFom
        }
    private fun Refusjonskrav.createAndreTrekkRequest(melding: Melding): OpprettAndreTrekkRequest {
        val prioritetFom = prioritetFom
        val tpKredMap = melding.kredCodes
        //TODO: Melding should contain TPNR, convert to TSS ekstern ID here.
        return OpprettAndreTrekkRequest(
            periodisertBelopListe.map {
                AndreTrekk(pid, melding.tssEksternId, prioritetFom, tpKredMap, it)
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

    private val Melding.kredCodes: TPKredMap
        get() = kredMapRepository.findByTssEksternIdFkAndUnderArt(tssEksternId, vedtak.underArt)
            ?: TODO("Feil ved henting av krediteringsmap")
    private val Melding.tssEksternId
        get() = tpClient.getTssEksternId(tpNr)

    private val Vedtak.underArt: ArtType
        get() = if (art == ArtType.UFOREP && !dateFom.isBefore(LocalDate.of(2015, 1, 1))) ArtType.UFOREUT
        else art


    companion object {
        const val YEAR_ADD_FACTOR: Long = 200
    }
}
