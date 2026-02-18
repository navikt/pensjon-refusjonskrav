package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.*
import no.nav.pensjon.refusjonskrav.domain.VedtakStatus.IKKE_OVERFORT_PEN
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.AndreTrekk
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.OpprettAndreTrekkRequest
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import no.nav.pensjon.refusjonskrav.repository.TPKredMapRepository
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
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
        val melding = samClient.hentMelding(refusjonskrav.pid, refusjonskrav.samId)

        when {
            melding.tpnr != refusjonskrav.tpNr -> TODO("Avvik hvis melding ikke samsvarer med kravet.")
            melding.meldingStatus == MeldingStatus.BESVART || melding.vedtak.vedtakStatus == VedtakStatus.BESVART -> TODO(
                "Kaste exception hvis status 'BESVART'."
            )

            refusjonskrav.periodisertBelopListe.isEmpty() -> {
                registrerSvar(melding, false)
                return
            }

            else -> {
                refusjonskrav.periodisertBelopListe.forEach { refusjonstrekk ->
                    when {
                        refusjonstrekk.datoFom.toLocalDate().isBefore(melding.vedtak.dateFom) -> TODO("Kast exception.")
                        refusjonstrekk.datoTom.toLocalDate().isBefore(melding.vedtak.dateFom) -> TODO("Kast exception.")
                        melding.vedtak.dateTom?.isBefore(refusjonstrekk.datoTom.toLocalDate()) == true -> TODO("Kast exception.")
                        melding.vedtak.dateTom == null && lastDayOfNextMonth.isBefore(refusjonstrekk.datoTom.toLocalDate()) -> TODO("Kast exception.")
                    }
                }
            }
        }

        samClient.opprettHendelse(refusjonskrav.pid, refusjonskrav.tpNr)


        registrerSvar(melding, refusjonskrav.refusjonskrav)

        refusjonskrav.createAndreTrekkRequest(melding).also { andreTrekkRequest ->
            osClient.opprettAndreTrekk(andreTrekkRequest)
        }

        if (melding.vedtak.samordningMeldingListe.all { !it.meldingStatus.erBesvart })
            avsluttBehandling(melding.vedtak)
    }

    private fun avsluttBehandling(vedtak: Vedtak) {
        if (vedtak.vedtakStatus != IKKE_OVERFORT_PEN)
            TODO("Oppdater samordningVedtak med IKKE_OVERFORT_PEN i database")
            //samCLient.oppdaterSamVedtak(fnr, samVedtakid, IKKE_OVERFORT_PEN)

        if (vedtak.vedtakStatus == IKKE_OVERFORT_PEN)
            vedtakService.lukkVedtak(vedtak)

        TODO("Oppdater samordningVedtak med BESVART i database")
    }

    private fun registrerSvar(melding: Melding, refusjonskrav: Boolean) {
        melding.refusjonskrav = refusjonskrav
        melding.datoSvart = LocalDate.now()
        melding.meldingStatus = MeldingStatus.BESVART
        samClient.lagreMelding(melding)
    }

    private val Refusjonskrav.prioritetFom
        get() = tpClient.getYtelser(pid, tpNr).run {
            if (onlyAndresYtelser) prioritetFom.plusYears(YEAR_ADD_FACTOR) else prioritetFom
        }
    private fun Refusjonskrav.createAndreTrekkRequest(melding: Melding): OpprettAndreTrekkRequest {
        val prioritetFom = prioritetFom
        val tpKredMap = melding.kredCodes
        return OpprettAndreTrekkRequest(
            periodisertBelopListe.map {
                AndreTrekk(pid, melding.tssEksternId, prioritetFom, tpKredMap, it)
            }
        )
    }

    private val Set<Ytelse>.prioritetFom: LocalDate
        get() = last().innmeldtFom //TODO This must be wrong!
    private val Set<Ytelse>.onlyAndresYtelser
        get() = all { it.ytelseKode == "GJENLEVENDE" || it.ytelseKode == "BARN" }

    private val Melding.kredCodes: TPKredMap
        get() = kredMapRepository.findByTssEksternIdFkAndUnderArt(tssEksternId, vedtak.underArt)
            ?: TODO("Feil ved henting av krediteringsmap")

    private val Melding.tpnr: String
        get() = tpClient.getTpnr(tssEksternId)

    private val Vedtak.underArt: ArtType
        get() = if (art == ArtType.UFOREP && !dateFom.isBefore(LocalDate.of(2015, 1, 1))) ArtType.UFOREUT
        else art


    companion object {
        const val YEAR_ADD_FACTOR: Long = 200
    }
}
