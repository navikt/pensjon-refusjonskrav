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
                logger.info("Opprettet refusjonskrav ok")
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
            HttpStatus.NOT_FOUND -> {
                logger.warn("Melding not found: $samId.", e)
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningMelding ikke funnet.", e)
            }
            else -> {
                logger.error("Failed to get melding: $samId.", e)
                throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
            }
        }
    } catch (e: RestClientException) {
        logger.error("Failed to get melding: $samId.", e)
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
    }

    fun updateMelding(melding: Melding, refusjonskrav: Boolean, datoSvart: LocalDate, status: MeldingStatus) = try {
        logger.info("Updating melding: ${melding.samId}, refusjonskrav: $refusjonskrav, datoSvart: $datoSvart, status: $status.")
        samRestTemplate.patchForObject<Melding>("/api/melding/${melding.samId}/status", UpdateMeldingRequest(
            refusjonskrav,
            datoSvart,
            status
        ))
    } catch (e: HttpStatusCodeException) {
        logger.error("Failed to update melding: ${melding.samId}.", e)
        when (e.statusCode) {
            HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningMelding ikke funnet.", e)
            else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    } catch (e: RestClientException) {
        logger.error("Failed to update melding: ${melding.samId}.", e)
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
    }

    fun opprettHendelse(fnr: String, tpnr: String) {
        try {
            logger.debug("Creating hendelse.")
            samRestTemplate.postForObject<Nothing?>("/api/hendelse",
                OpprettHendelseRequest(
                    fnr = fnr,
                    tpnr = tpnr
                )
            )
        }  catch (e: RestClientException) {
            logger.error("Failed to create hendelse.", e)
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    }

    fun oppdaterVedtak(vedtak: Vedtak, status: VedtakStatus) {
        try {
            logger.info("Updating vedtak: ${vedtak.samVedtakId}, status: $status.")
            samRestTemplate.patchForObject<Nothing?>("/api/vedtak/${vedtak.samVedtakId}", UpdateVedtakRequest(status))
        } catch (e: HttpStatusCodeException) {
            logger.error("Failed to update vedtak: ${vedtak.samVedtakId}.", e)
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningVedtak ikke funnet.", e)
                else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
            }
        } catch (e: RestClientException) {
            logger.error("Failed to update vedtak: ${vedtak.samVedtakId}.", e)
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message)
        }
    }
}
