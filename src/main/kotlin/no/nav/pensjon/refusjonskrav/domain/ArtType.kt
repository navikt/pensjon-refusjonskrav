package no.nav.pensjon.refusjonskrav.domain

import no.nav.pensjon.refusjonskrav.domain.TrekkGruppe.*
import no.nav.pensjon.refusjonskrav.domain.TrekkType.*

enum class ArtType(val trekkGruppe: TrekkGruppe, val trekkType: TrekkType) {

    /**
     * Arbeidsavklaringspenger
     */
    AAP(
        trekkGruppe = TrekkGruppe.AAP,
        trekkType = RTPO
    ),

    /**
     * Alderspensjon
     */
    ALDER(
        trekkGruppe = PEAP,
        trekkType = RAPE
    ),

    /**
     * Gjenlevendeytelse
     */
    GJENLEV(
        trekkGruppe = PEGJ,
        trekkType = RGJE
    ),

    /**
     * Uføretrygd
     */
    UFOREP(
        trekkGruppe = PEUP,
        trekkType = RUPE
    ),

    /**
     * AFP
     */
    AFP(
        trekkGruppe = PEAF,
        trekkType = RAFE
    ),

    /**
     * Barnepensjon
     */
    BARNEP(
        trekkGruppe = PEBP,
        trekkType = RBPE
    ),

    /**
     * Krigspensjon
     */
    KRIGP(
        trekkGruppe = PEKP,
        trekkType = RKPE
    ),

    /**
     * Gammel yrkesskade
     */
    GAM_YRK(
        trekkGruppe = PEGY,
        trekkType = RGYE
    ),

    /**
     * AFP Privat
     */
    AFP_PRIVAT(
        trekkGruppe = PEPP,
        trekkType = RPPE
    ),

    /**
     * Familiepleierytelse
     */
//    FAM_PL(
//
//    ),
    /** Omstillingstønad
     *
     */
    OMS(
        trekkGruppe = OMST,
        trekkType = ROME
    ),
    /**
     * Samordningspliktige ytelser for AFP Offentlig
     *
     */
//    OPPSATT_BTO_PEN,
//    SAERALDER,
    /**
     * UFOREP etter 01-01-2015.
     */
    UFOREUT(
        trekkGruppe = UFORE,
        trekkType = RUTE
    );
}
