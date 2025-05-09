package no.nav.pensjon.refusjonskrav.domain.okonomi

data class Fagomrade(
    var trekkgruppeKode: String
) {
    var fagomradeKode: String? = null
    var gyldig: String? = null
}
