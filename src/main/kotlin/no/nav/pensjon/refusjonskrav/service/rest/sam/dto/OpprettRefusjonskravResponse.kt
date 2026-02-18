package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

data class OpprettRefusjonskravResponse(
    val message: String? = null,
    val exceptionType: OpprettRefusjonskravExceptions? = null
)
