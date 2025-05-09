package no.nav.pensjon.refusjonskrav.domain

import jakarta.persistence.*
import no.nav.pensjon.refusjonskrav.domain.support.ChangeStamp
import java.util.*

data class TPKredMap(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "K_TP_KRED_MAP_ID")
    val tpKredMapId: Long = 0,

    @Column(name = "TSS_EKSTERN_ID_FK")
    val tssEksternIdFk: String,

    @Column(name = "DEKODE", nullable = false)
    val dekode: String,

    @Column(name = "TREKKTYPE")
    val trekkType: String,

    @Column(name = "DATO_FOM", nullable = false)
    val dateFom: Date,

    @Column(name = "TREKKGRUPPE")
    val trekkGruppe: String,

    @Column(name = "DATO_TOM")
    val datoTom: Date,

    @Column(name = "TSS_EKSTERN_ID_KRE")
    val tssEksternIdKre: String,

    @Column(name = "ER_GYLDIG", nullable = false)
    val erGyldig: Boolean,

    @Column(name = "K_UNDER_ART", nullable = false)
    val underArt: ArtType
) {

    @Embedded
    val changeStamp: ChangeStamp = ChangeStamp(TODO("Get user from security context."))
}
