package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import no.nav.pensjon.refusjonskrav.domain.UnderArt

enum class ArtTypeCode(val underArt: UnderArt?) {
    /**
     * Alderspensjon
     */
    ALDER(UnderArt.ALDER),

    /**
     * Gjenlevendeytelse
     */
    GJENLEV(UnderArt.GJENLEV),

    /**
     * Uføretrygd
     */
    UFOREP(UnderArt.UFOREP),

    /**
     * AFP
     */
    AFP(UnderArt.AFP),

    /**
     * Barnepensjon
     */
    BARNEP(UnderArt.BARNEP),

    /**
     * Krigspensjon
     */
    KRIGP(UnderArt.KRIGP),

    /**
     * Gammel yrkesskade
     */
    GAM_YRK(UnderArt.GAM_YRK),

    /**
     * AFP Privat
     */
    AFP_PRIVAT(UnderArt.AFP_PRIVAT),

    /**
     * Familiepleierytelse
     */
    FAM_PL(null),

    /** Omstillingstønad
     *
     */
    OMS(UnderArt.OMS),

    /**
     * Samordningspliktige ytelser for AFP Offentlig
     *
     */
    OPPSATT_BTO_PEN(null),
    SAERALDER(null),

    /**
     * Kelvin (nye Arena)
     */
    AAP(UnderArt.AAP);
}