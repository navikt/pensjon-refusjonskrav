package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

enum class OpprettRefusjonskravExceptions(val status: HttpStatus) {
    ALLEREDE_REGISTRERT_ELLER_UTENFOR_FRIST(HttpStatus.CONFLICT),
    ELEMENT_FINNES_IKKE(HttpStatus.NOT_FOUND),
    ULOVLIG_TREKK(HttpStatus.BAD_REQUEST),
    FUNKSJONELL(HttpStatus.INTERNAL_SERVER_ERROR),
    GENERELL(HttpStatus.INTERNAL_SERVER_ERROR);

    fun throwResponseStatusException(message: String?): Nothing = throw ResponseStatusException(status, message)
}
