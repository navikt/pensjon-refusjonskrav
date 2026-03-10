package no.nav.pensjon.refusjonskrav.service.rest.pen

import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import org.springframework.web.server.ResponseStatusException

@Service
class PenClient(
    private val penRestTemplate: RestTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        try {
            penRestTemplate.getForObject<String>("/actuator/health/readiness")
        } catch (e: RestClientException) {
            logger.error("PEN unavailable.", e)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to ping PEN: ${e.message}", e)
        }
    }

    fun lukkVedtak(vedtak: Vedtak) {
        logger.info("Closing vedtak: ${vedtak.samVedtakId}")
        try {
            penRestTemplate.postForObject<Unit>("/api/vedtak/${vedtak.fagVedtakId}/mottaSamhandlerSvar")
        } catch (e: Exception) {
            logger.error("Failed to close vedtak: ${vedtak.samVedtakId}.", e)
            throw e
        }
    }
}
