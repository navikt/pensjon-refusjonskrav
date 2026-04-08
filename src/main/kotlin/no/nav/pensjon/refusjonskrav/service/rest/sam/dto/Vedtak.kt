package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import no.nav.pensjon.refusjonskrav.domain.UnderArt
import java.time.LocalDate

class Vedtak(
    val samVedtakId: Long,
    val person: String,
    val fagomrade: Fagomrade,
    val fagVedtakId: Long,
    var vedtakStatus: VedtakStatus,
    val art: ArtTypeCode,
    val alleMeldingerBesvart: Boolean,
    val dateFom: LocalDate,
    val dateTom: LocalDate?
)
