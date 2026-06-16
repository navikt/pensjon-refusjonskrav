package no.nav.pensjon.refusjonskrav.service.kafka

import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak

data class LukkVedtakMelding(
    val fagomrade: String,
    val artTypeKode: String,
    val vedtakId: Long
) {
    constructor(vedtak: Vedtak) : this(
        fagomrade = vedtak.fagomrade.name,
        artTypeKode = vedtak.art.name,
        vedtakId = vedtak.fagVedtakId
    )
}
