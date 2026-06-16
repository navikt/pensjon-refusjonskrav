package no.nav.pensjon.refusjonskrav.service.rest.okonomi

import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.OpprettAndreTrekkRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException

@Service
class OsClient(
    private val osRestTemplate: RestTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        try {
            osRestTemplate.getForObject<String>("/actuator/health/readiness")
        } catch (e: RestClientException) {
            logger.error("OS unavailable.", e)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to ping OS.")
        }
    }

    fun opprettAndreTrekk(request: OpprettAndreTrekkRequest) {
        try {
            logger.info("Calling OS for AndreTrekk")
            osRestTemplate.postForLocation("/api/nav-cons-sto-sam-trekk/opprettAndreTrekk", request)
        }  catch (e: RestClientException) {
            logger.error("Failed to create AndreTrekk.", e)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, "Opprett AndreTrekk failed", e)
        }
    }
}
