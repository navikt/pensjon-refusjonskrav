package no.nav.pensjon.refusjonskrav.domain

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class Refusjonskrav(
    @field:Pattern(regexp = "\\d{11}", message = "pid must be exactly 11 digits.")
    val pid: String,
    @field:Pattern(regexp = "\\d{4}", message = "tpnr must be exactly 4 digits.")
    val tpNr: String,
    val samId: Long,
    val refusjonskrav: Boolean,
    @field:Valid
    val periodisertBelopListe: List<Refusjonstrekk>
) {
    override fun toString(): String {
        return "tpnr: $tpNr, samId: $samId, ref: $refusjonskrav"
    }
}
