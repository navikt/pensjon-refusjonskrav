package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.*
import no.nav.pensjon.refusjonskrav.domain.okonomi.AndreTrekk
import no.nav.pensjon.refusjonskrav.domain.okonomi.OpprettAndreTrekkRequest
import no.nav.pensjon.refusjonskrav.domain.tp.Ytelse
import no.nav.pensjon.refusjonskrav.repository.TPKredMapRepository
import java.time.LocalDate

//@Service
internal class RefusjonskravService(val samClient: SamClient, val tpClient: TpClient, val kredMapRepository: TPKredMapRepository) {

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
                        refusjonstrekk.datoFom.isBefore(melding.vedtak.dateFom) -> TODO("Kast exception.")
                        refusjonstrekk.datoTom.isBefore(melding.vedtak.dateFom) -> TODO("Kast exception.")
                        melding.vedtak.dateTom?.isBefore(refusjonstrekk.datoTom) == true -> TODO("Kast exception.")
                        melding.vedtak.dateTom == null && lastDayOfNextMonth.isBefore(refusjonstrekk.datoTom) -> TODO("Kast exception.")
                    }
                }
            }
        }

        samClient.opprettHendelse(refusjonskrav.pid, refusjonskrav.tpNr)


        registrerSvar(melding, refusjonskrav.refusjonskrav)

        refusjonskrav.createAndreTrekkRequest(melding)
        /*
         * TODO Kaller OS for å opprette andre trekk.
         * rest/soap?
         * osClient.opprettAndreTrekk(trekkRequest)
         */

        if (melding.vedtak.samordningMeldingListe.all { !it.meldingStatus.erBesvart })
            TODO("AvsluttBehandlingSendMeldingLukkVedtak")

        //TODO oppdater samordningVedtak med IKKE_OVERFORT_PEN i database.
        //samCLient.oppdaterSamVedtak(fnr, samVedtakid, "IKKE_OVERFORT_PEN")


        //TODO send penClient hvis PEN eller kafka hvis EYO
        //oppdater samordningVedtak med BESVART i db.
        //svar til PEN(REST) hvis ikke EYO
        //        ellers til kafka

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
