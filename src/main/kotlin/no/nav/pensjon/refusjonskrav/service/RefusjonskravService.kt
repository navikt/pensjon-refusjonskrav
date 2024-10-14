package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav

//@Service
internal class RefusjonskravService {


    fun behandleRefusjonskrav(refusjonskrav: Refusjonskrav) {

        //TODO Validere refisjonkrav med Periodiskebeløp

        //TODO tjeneste for å hente SamPerson - med valgt samVedtak
        //val samPerson/samVedtak = samClient.hentPerson(refusjonskrav.pid, refusjonskrav.samId)

        //TODO Kaste Exception hvis vedtak ikke funnet eller status "BESVART"

        //TODO Sjekker om refusjonstrekk i periodiserte beløp er innenfor vedtakets fom og tom

        //TODO Oppretter og lagrer samordningshendelse på person
        //samClient.opprettHendelse()

        //TODO hent TP forholdListe med ytelser
        //TODO Setter prioritet dersom ytelse barnepensjon eller gjenlevende
        //prioritertDate + 200years

        //TODO henter kredittLinjs fra tabel TPKredMapCodes (kan flyttes over til hit)
        //val tpKredCodes = samCLient.hentKredMapCodes(tssid, ytel)

        //TODO Setter samordningsmelding til besvart og lagrer.
        //samCLient.oppdatertSamMelding("BESVART", fnr.. )

        //TODO Kaller OS for å opprette andre trekk.
        //rest/soap?
        //osClient.opprettAndreTrekk(trekkRequest)


        //TODO sjekker om alle svar
        //input: fagvedtakId
        //sjekker om alle svar er mottat for samordningsmeldinger på samordningsvedtak

        //TODO oppdater samordningVedtak med IKKE_OVERFORT_PEN i database.
        //samCLient.oppdaterSamVedtak(fnr, samVedtakid, "IKKE_OVERFORT_PEN")


        //TODO send penClient hvis PEN eller kafka hvis EYO
        //oppdater samordningVedtak med BESVART i db.
        //svar til PEN(REST) hvis ikke EYO
        //        ellers til kafka

    }
}