package no.nav.pensjon.refusjonskrav.domain

data class Refusjonskrav(
    val pid: String,
    val tpNr: String,
    val samId: Long,
    val refusjonskrav: Boolean,
    val periodisertBelopListe: List<Refusjonstrekk>
)
