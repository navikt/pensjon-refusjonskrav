package no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto

data class Fagomrade(
    var trekkgruppeKode: String
) {
    var fagomradeKode: String? = null
    var gyldig: String? = null
}
