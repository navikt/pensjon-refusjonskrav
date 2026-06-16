package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import java.time.LocalDate

data class Vedtak(
    val samVedtakId: Long,
    val person: String,
    val fagomrade: Fagomrade,
    val fagVedtakId: Long,
    val vedtakStatus: VedtakStatus,
    val art: ArtTypeCode,
    val alleMeldingerBesvart: Boolean,
    val dateFom: LocalDate,
    val dateTom: LocalDate?
)
