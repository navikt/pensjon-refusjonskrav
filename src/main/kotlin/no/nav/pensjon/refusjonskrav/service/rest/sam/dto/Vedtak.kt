package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import java.time.LocalDate

class Vedtak(
    val samVedtakId: Long,
    val fagomrade: Fagomrade,
    val fagVedtakId: Long,
    var vedtakStatus: VedtakStatus,
    val art: ArtType,
    val alleMeldingerBesvart: Boolean,
    val dateFom: LocalDate,
    val dateTom: LocalDate?
)
