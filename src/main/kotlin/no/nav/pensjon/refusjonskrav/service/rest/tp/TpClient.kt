package no.nav.pensjon.refusjonskrav.service.rest.tp

import no.nav.pensjon.refusjonskrav.exception.RefusjonskravErrorResponseException.OrdningForbiddenException
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.OrdningDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.PersonDto
import no.nav.pensjon.refusjonskrav.service.rest.tp.dto.Ytelse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Service
import org.springframework.web.client.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Service
class TpClient(
    private val tpRestTemplate: RestTemplate
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    //TODO("Bedre endepunkt kommer i TP API v2, bruk av QUERY.")
    fun getYtelser(fnr: String, tpnr: String): Set<Ytelse> = try {
        tpRestTemplate.getForObject<PersonDto>(
            UriComponentsBuilder.fromPath("/api/finnForholdForBruker")
                .queryParam("fnr", fnr)
                .queryParam("tpnr", tpnr)
                .build().toUri().toString()
        ).forhold.first().ytelser
    } catch (e: RestClientException) {
        logger.error("TP unavailable.", e)
        throw ResponseStatusException(
            BAD_GATEWAY
        )
    }

    fun getTssEksternId(tpnr: String) = try {
        tpRestTemplate.getForObject<OrdningDto>("/api/ordning?tpnr=$tpnr").tssId!!
    } catch (e: RestClientException) {
        logger.error("TP unavailable.", e)
        throw ResponseStatusException(BAD_GATEWAY)
    }

    fun validateTpnr(tpnr: String, orgno: String): Boolean {
        try {
            logger.info("Maskinporten token received. Validating tpnr: $tpnr is managed by orgno: $orgno")
            return doValidation(tpnr, orgno)
        } catch (e: HttpStatusCodeException) {
            if (e.statusCode == NOT_FOUND) throw OrdningForbiddenException(tpnr, orgno)
            else {
                logger.error("Unexpected response from TP on tpnr validation.", e)
                throw ResponseStatusException(BAD_GATEWAY, "Unexpected response from TP on tpnr validation.", e)
            }
        } catch (e: RestClientException) {
            logger.error("Unexpected error from TP on tpnr validation.", e)
            throw ResponseStatusException(BAD_GATEWAY, "Unexpected error from TP on tpnr validation.", e)
        }
    }

    private fun doValidation(tpnr: String, orgno: String) = tpRestTemplate.getForObject<Boolean>("/api/tpconfig/organisation/validate/" + tpnr + "_" + orgno)

    fun ping() {
        try {
            tpRestTemplate.getForEntity<String>("/actuator/health/readiness")
        } catch (e: RestClientException) {
            throw ResponseStatusException(SERVICE_UNAVAILABLE, "Failed to ping TP: ${e.message}", e)
        }
    }
}
