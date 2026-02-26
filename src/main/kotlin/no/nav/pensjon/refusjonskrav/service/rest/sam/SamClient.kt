package no.nav.pensjon.refusjonskrav.service.rest.sam

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

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

    fun hentMelding(samId: Long) = try {
        samRestTemplate.getForObject<Melding>("/api/melding/$samId")
    } catch (e: HttpStatusCodeException) {
        when (e.statusCode) {
            HttpStatus.NOT_FOUND -> TODO("Throw exception for melding ikke funnet.")
            else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    } catch (e: RestClientException) {
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
    }

    fun updateMelding(melding: Melding, refusjonskrav: Boolean, datoSvart: LocalDate, status: MeldingStatus): Melding {
        samRestTemplate.patchForObject<Unit>("api/melding/${melding.samId}", UpdateMeldingRequest(
            refusjonskrav,
            datoSvart,
            status
        ))
        return hentMelding(melding.samId)
    }

    fun opprettHendelse(fnr: String, tpnr: String) {
        TODO("Opprett refusjonskrav hendelse i SAM.")
    }

    fun oppdaterVedtak(vedtak: Vedtak, status: VedtakStatus) {
        try {
            samRestTemplate.patchForObject<Unit>("/api/vedtak/${vedtak.fagVedtakId}/status", UpdateVedtakRequest(status))
            vedtak.vedtakStatus = status
        } catch (e: HttpStatusCodeException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message)
        }
    }
}
