package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class SamClient(
    private val samRestTemplate: RestTemplate
) {

    fun opprettRefusjonskrav(refusjonskrav: Refusjonskrav): Boolean {
        try {
            val response = samRestTemplate.postForEntity("/api/refusjonskrav", refusjonskrav, OpprettRefusjonskravResponse::class.java).body!!
            return when {
                !response.refusjonskravAlleredeRegistrertEllerUtenforFrist && response.exception == null && response.exceptionName == null -> true
                response.refusjonskravAlleredeRegistrertEllerUtenforFrist -> throw ResponseStatusException(HttpStatus.CONFLICT, response.exception?.message)
                else -> {
                    throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, response.exception?.message)
                }
            }
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}