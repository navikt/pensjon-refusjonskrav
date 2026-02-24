package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

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
