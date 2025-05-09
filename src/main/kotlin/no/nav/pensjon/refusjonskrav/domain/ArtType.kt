package no.nav.pensjon.refusjonskrav.domain

enum class ArtType {

    /**
     * Alderspensjon
     */
    ALDER,

    /**
     * Gjenlevendeytelse
     */
    GJENLEV,

    /**
     * Uføretrygd
     */
    UFOREP,

    /**
     * AFP
     */
    AFP,

    /**
     * Barnepensjon
     */
    BARNEP,

    /**
     * Krigspensjon
     */
    KRIGP,

    /**
     * Gammel yrkesskade
     */
    GAM_YRK,

    /**
     * AFP Privat
     */
    AFP_PRIVAT,

    /**
     * Familiepleierytelse
     */
    FAM_PL,
    /** Omstillingstønad
     *
     */
    OMS,
    /**
     * Samordningspliktige ytelser for AFP Offentlig
     *
     */
    OPPSATT_BTO_PEN,
    SAERALDER,
    /**
     * UFOREP etter 01-01-2015.
     */
    UFOREUT;
}
