package no.nav.pensjon.refusjonskrav.service.rest.tp

import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.OrdningDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException

@Service
class TpClient(
    private val tpRestTemplate: RestTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun getYtelser(fnr: String, tpnr: String): Set<Ytelse> {
        TODO("Lag bedre endepunkt i TP")
    }

    fun getTpnr(tssId: String) = try {
        tpRestTemplate.getForObject<OrdningDto>("/api/ordning?tssId=$tssId").tpNr
    } catch (e: RestClientException) {
        logger.error("TP unavailable.", e)
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY)
    }

    fun ping() {
        try {
            tpRestTemplate.getForEntity<String>("/actuator/health/readiness")
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to ping TP: ${e.message}", e)
        }
    }
}
