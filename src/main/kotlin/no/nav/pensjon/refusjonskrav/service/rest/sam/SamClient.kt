package no.nav.pensjon.refusjonskrav.service.rest.sam

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.*
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.HendelseType.REFUSJONSKRAV
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.KanalType.WEB_SERVICE
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
            HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningMelding ikke funnet.", e)
            else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    } catch (e: RestClientException) {
        throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
    }

    fun updateMelding(melding: Melding, refusjonskrav: Boolean, datoSvart: LocalDate, status: MeldingStatus): Melding {
        try {
            samRestTemplate.patchForObject<Unit>("/api/melding/${melding.samId}/status", UpdateMeldingRequest(
                refusjonskrav,
                datoSvart,
                status
            ))
        } catch (e: HttpStatusCodeException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningMelding ikke funnet.", e)
                else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
            }
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
        return hentMelding(melding.samId)
    }

    fun opprettHendelse(fnr: String, tpnr: String) {
        try {
            samRestTemplate.postForObject<Unit>("/api/hendelse",
                OpprettHendelseRequest(
                    fnr = fnr,
                    tpnr = tpnr,
                    hendelseType = REFUSJONSKRAV,
                    kanalType = WEB_SERVICE
                )
            )
        } catch (e: HttpStatusCodeException) {
            when (e.statusCode) {
                else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
            }
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
        }
    }

    fun oppdaterVedtak(vedtak: Vedtak, status: VedtakStatus) {
        try {
            samRestTemplate.patchForObject<Unit>("/api/vedtak/${vedtak.samVedtakId}", UpdateVedtakRequest(status))
            vedtak.vedtakStatus = status
        } catch (e: HttpStatusCodeException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, "SamordningVedtak ikke funnet.", e)
                else -> throw ResponseStatusException(HttpStatus.BAD_GATEWAY, e.message)
            }
        } catch (e: RestClientException) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.message)
        }
    }
}
