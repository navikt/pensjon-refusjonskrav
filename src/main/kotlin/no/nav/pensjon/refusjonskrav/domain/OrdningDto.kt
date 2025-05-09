package no.nav.pensjon.refusjonskrav.domain

import com.fasterxml.jackson.annotation.JsonInclude

data class OrdningDto(
    val navn: String,
    val tpNr: String,
    val orgNr: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val tssId: String? = null,
    val alias: List<String> = emptyList()
)
