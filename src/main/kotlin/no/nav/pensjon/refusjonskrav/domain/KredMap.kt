package no.nav.pensjon.refusjonskrav.domain

class KredMap(
    val tssIdKre: String,
    private val trekkTyper: Map<ArtType, TrekkType>
) {

    fun getTrekkType(artType: ArtType): TrekkType = trekkTyper[artType] ?: artType.trekkType
}
