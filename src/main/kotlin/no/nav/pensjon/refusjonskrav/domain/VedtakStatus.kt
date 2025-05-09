package no.nav.pensjon.refusjonskrav.domain

enum class VedtakStatus {

    /**
     * Status opprettet
     */
    OPPRETTET,

    /**
     * Sendt
     */
    SENDT,

    /**
     * Besvart eller lukket
     */
    BESVART,

    /**
     * Timet ut
     */
    TIMET_UT,

    /**
     * Slettet eller kanselert
     */
    KANSELERT,

    /**
     * Ikke overført til PEN
     */
    IKKE_OVERFORT_PEN;
}
