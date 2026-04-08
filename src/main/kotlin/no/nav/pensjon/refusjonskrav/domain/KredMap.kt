package no.nav.pensjon.refusjonskrav.domain

class KredMap(
    val tssIdKre: String,
    private val trekkTyper: Map<UnderArt, TrekkType>
) {

    fun getTrekkType(underArt: UnderArt): TrekkType = trekkTyper[underArt] ?: underArt.trekkType
}
