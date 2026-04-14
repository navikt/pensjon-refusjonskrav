package no.nav.pensjon.refusjonskrav.domain

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern

data class Refusjonskrav(
    @Deprecated("Not needed.")
    @field:Pattern(regexp = "\\d{11}", message = "pid (deprecated) must be exactly 11 digits or null.")
    val pid: String? = null,
    @Deprecated("Not needed.")
    @field:Pattern(regexp = "\\d{4}", message = "tpnr (deprecated) must be exactly 4 digits or null.")
    val tpNr: String? = null,
    val samId: Long,
    val refusjonskrav: Boolean,
    @field:Valid
    val periodisertBelopListe: List<Refusjonstrekk>
) {
    override fun toString(): String {
        return "tpnr: $tpNr, samId: $samId, ref: $refusjonskrav"
    }
}
