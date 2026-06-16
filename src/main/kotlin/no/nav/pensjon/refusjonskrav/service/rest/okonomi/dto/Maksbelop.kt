package no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto

data class Maksbelop (
    var belopMax: String? = null,
    var periodeKode: String? = null,
    var gyldig: String? = null
)
