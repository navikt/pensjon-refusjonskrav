package no.nav.pensjon.refusjonskrav.domain

class KredMap(
    val tssIdKre: String,
    private val trekkTyper: Map<UnderArt, TrekkType> = emptyMap()
) {

    fun getTrekkType(underArt: UnderArt): TrekkType = trekkTyper[underArt] ?: underArt.trekkType
}
