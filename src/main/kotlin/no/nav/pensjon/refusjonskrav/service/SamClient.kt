package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class SamClient(
    private val samRestTemplate: RestTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun opprettRefusjonskrav(refusjonskrav: Refusjonskrav) {
        try {
            val response = samRestTemplate.postForEntity(
                "/api/refusjonskrav/",
                refusjonskrav,
                OpprettRefusjonskravResponse::class.java
            ).body!!.also {
                logger.info("opprettet refusjonskrav ok")
            }
            response.exceptionType?.throwResponseStatusException(response.message)
        } catch (e: RestClientException) {
            logger.error(e.message, e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }
    }
}