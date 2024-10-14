package no.nav.pensjon.refusjonskrav.service

data class OpprettRefusjonskravResponse(
    val refusjonskravAlleredeRegistrertEllerUtenforFrist: Boolean,
    val exception: Exception? = null,
    val exceptionName: String? = null
)
