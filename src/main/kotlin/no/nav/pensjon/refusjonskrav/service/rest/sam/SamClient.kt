package no.nav.pensjon.refusjonskrav.service.rest.sam

import no.nav.pensjon.refusjonskrav.domain.Melding
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.OpprettRefusjonskravResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import org.springframework.web.server.ResponseStatusException

@Service
class SamClient(
    private val samRestTemplate: RestTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        try {
            samRestTemplate.getForEntity<String>("/actuator/health/readiness")
        } catch (e: RestClientException) {
            logger.error("SAM unavailable.", e)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to ping SAM: ${e.message}", e)
        }
    }

    fun opprettRefusjonskrav(refusjonskrav: Refusjonskrav) {
        ping()
        try {
            val response = samRestTemplate.postForEntity<OpprettRefusjonskravResponse>(
                "/api/refusjonskrav",
                refusjonskrav
            ).body!!.also {
                logger.info("opprettet refusjonskrav ok")
            }
            response.exceptionType?.throwResponseStatusException(response.message)
        } catch (e: HttpStatusCodeException) {
            throw ResponseStatusException(e.statusCode, e.message)
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    }

    fun hentMelding(pid: String, samId: Long): Melding {
        TODO("Hent melding fra SAM. Kaste exception hvis ikke funnet.")
    }

    fun lagreMelding(melding: Melding) {
        TODO("Lagre melding.")
    }

    fun opprettHendelse(fnr: String, tpnr: String) {
        TODO("Opprett refusjonskrav hendelse i SAM.")
    }
}
