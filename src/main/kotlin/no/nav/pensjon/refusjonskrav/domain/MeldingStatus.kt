package no.nav.pensjon.refusjonskrav.domain

enum class MeldingStatus {

    /**
     * Opprettet melding
     */
    OPPRETTET,

    /**
     * Sendt
     */
    SENDT,

    /**
     * Besvart
     */
    BESVART,

    /**
     * Purring
     */
    PURRET,

    /**
     * kansellert
     */
    KANSELLERT;

    val erBesvart: Boolean
        get() = this == BESVART || this == KANSELLERT
}
