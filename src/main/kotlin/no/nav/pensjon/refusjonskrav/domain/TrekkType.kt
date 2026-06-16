package no.nav.pensjon.refusjonskrav.domain

enum class TrekkType {

    /**
     * Arbeidsavklaringspenger (?)
     */
    RTPO,

    /**
     * Alderspensjon
     */
    RAPE,

    /**
     * Gjenlevendeytelse
     */
    RGJE,

    /**
     * Uføretrygd
     */
    RUPE,

    /**
     * AFP
     */
    RAFE,

    /**
     * Barnepensjon
     */
    RBPE,

    /**
     * Krigspensjon
     */
    RKPE,

    /**
     * Gammel yrkesskade
     */
    RGYE,

    /**
     * AFP Privat
     */
    RPPE,

    /**
     * Familiepleierytelse
     */
//    FAM_PL(
//
//    ),
    /** Omstillingstønad
     *
     */
    ROME,
    /**
     * Samordningspliktige ytelser for AFP Offentlig
     *
     */
//    OPPSATT_BTO_PEN,
//    SAERALDER,
    /**
     * UFOREP etter 01-01-2015.
     */
    RUTE,

    RS30,
    RS31,
    RS32,
    RS33,
    RS34,
    RS35,
    RS36,
    RS37,
    RS38,
    RSAA,

    RPTS;
}
