package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import java.time.LocalDate

data class Melding(
    val samId: Long,
    val vedtak: Vedtak,
    var datoSvart: LocalDate? = null,
    val tpNr: String,
    var refusjonskrav: Boolean? = null,
    var meldingStatus: MeldingStatus
) {
    val pid: String
        get() = vedtak.person
}
