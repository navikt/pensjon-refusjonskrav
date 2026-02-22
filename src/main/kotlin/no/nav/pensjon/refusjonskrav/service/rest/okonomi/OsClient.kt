package no.nav.pensjon.refusjonskrav.service.rest.okonomi

import no.nav.pensjon.refusjonskrav.service.rest.okonomi.dto.OpprettAndreTrekkRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.server.ResponseStatusException

@Service
class OsClient {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        try {
            TODO("Ping OS.")
        } catch (e: RestClientException) {
            logger.error("OS unavailable.", e)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to ping OS: ${e.message}", e)
        }
    }

    fun opprettAndreTrekk(request: OpprettAndreTrekkRequest) {
        TODO("Send request to OS")
    }
}
