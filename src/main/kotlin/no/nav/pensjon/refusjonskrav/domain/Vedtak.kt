package no.nav.pensjon.refusjonskrav.domain

import java.time.LocalDate

class Vedtak(
    val fagomrade: Fagomrade,
    val fagVedtakId: Long,
    val sakId: Long,
    val vedtakStatus: VedtakStatus,
    val art: ArtType,
    val purring: Boolean,
    val etterbetaling: Boolean,
    val samordningMeldingListe: Set<Melding>,
    val dateFom: LocalDate,
    val dateTom: LocalDate?
)
