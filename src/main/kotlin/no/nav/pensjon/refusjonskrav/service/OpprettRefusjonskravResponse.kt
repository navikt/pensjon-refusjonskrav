package no.nav.pensjon.refusjonskrav.service

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.web.server.ResponseStatusException

enum class OpprettRefusjonskravExceptions(val status: HttpStatus) {
    ALLEREDE_REGISTRERT_ELLER_UTENFOR_FRIST(CONFLICT),
    ELEMENT_FINNES_IKKE(NOT_FOUND),
    ULOVLIG_TREKK(BAD_REQUEST),
    FUNKSJONELL(INTERNAL_SERVER_ERROR),
    GENERELL(INTERNAL_SERVER_ERROR);

    fun throwResponseStatusException(message: String?): Nothing = throw ResponseStatusException(status, message)
}

data class OpprettRefusjonskravResponse(
    val message: String? = null,
    val exceptionType: OpprettRefusjonskravExceptions? = null
)