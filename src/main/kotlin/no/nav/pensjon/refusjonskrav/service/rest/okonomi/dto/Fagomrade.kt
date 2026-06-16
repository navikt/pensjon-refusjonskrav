package no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto

import no.nav.pensjon.refusjonskrav.domain.TrekkGruppe

data class Fagomrade(
    var trekkgruppeKode: TrekkGruppe
) {
    var fagomradeKode: String? = null
    var gyldig: String? = null
}
