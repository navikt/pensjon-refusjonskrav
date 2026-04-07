package no.nav.pensjon.refusjonskrav.domain

enum class TrekkGruppe {

    /**
     * Arbeidsavklaringspenger
     */
    AAP,

    /**
     * Alderspensjon
     */
    PEAP,

    /**
     * Gjenlevendeytelse
     */
    PEGJ,

    /**
     * Uføretrygd
     */
    PEUP,

    /**
     * AFP
     */
    PEAF,

    /**
     * Barnepensjon
     */
    PEBP,

    /**
     * Krigspensjon
     */
    PEKP,

    /**
     * Gammel yrkesskade
     */
    PEGY,

    /**
     * AFP Privat
     */
    PEPP,

    /**
     * Familiepleierytelse
     */
//    FAM_PL(
//
//    ),
    /** Omstillingstønad
     *
     */
    OMST,
    /**
     * Samordningspliktige ytelser for AFP Offentlig
     *
     */
//    OPPSATT_BTO_PEN,
//    SAERALDER,
    /**
     * UFOREP etter 01-01-2015.
     */
    UFORE;
}
