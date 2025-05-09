package no.nav.pensjon.refusjonskrav.domain

class SamPerson(
    val fnr: String,
    val samordningVedtakListe: Set<Vedtak>
)
