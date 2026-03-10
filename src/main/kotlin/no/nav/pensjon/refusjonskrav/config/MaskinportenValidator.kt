package no.nav.pensjon.refusjonskrav.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_GATEWAY
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.getForEntity
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException
import java.time.Duration.ofSeconds

@Service
class MaskinportenValidator(
    @Value("\${MASKINPORTEN_ISSUER}") private val maskinportenIssuer: String,
    @Value("\${tp.url}") tpUrl: String
) {

    private val restTemplate = RestTemplateBuilder().rootUri(tpUrl).readTimeout(ofSeconds(10)).build()
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun validateTpnrAuthorization(tpnr: String, token: JWT) {
        token.jwtClaimsSet.apply {
            if (issuer == maskinportenIssuer) {
                val tokenOrgno = getJSONObjectClaim("consumer")["ID"].toString().substringAfterLast(':')
                try {
                    log.info("Maskinporten token received. Validating tpnr: $tpnr is managed by orgno: $tokenOrgno")
                    if (fetchOrdning(tpnr).orgNr != tokenOrgno)
                            throw ResponseStatusException(FORBIDDEN, "tpnr: $tpnr is not managed by orgno: $tokenOrgno")
                } catch (e: HttpStatusCodeException) {
                    if (e.statusCode == NOT_FOUND) throw ResponseStatusException(NOT_FOUND, "Cannot find ordning $tpnr.")
                    else {
                        log.error("Unexpected response from TP on tpnr validation.", e)
                        throw ResponseStatusException(BAD_GATEWAY, "Unexpected response from TP on tpnr validation.", e)
                    }
                } catch (e: RestClientException) {
                    log.error("Unexpected error from TP on tpnr validation.", e)
                    throw ResponseStatusException(BAD_GATEWAY, "Unexpected error from TP on tpnr validation.", e)
                }
            }
        }
    }

    fun fetchOrdning(tpnr: String): OrdningDto = restTemplate.getForObject<OrdningDto>("/api/ordning/$tpnr")

    data class OrdningDto(
        val navn: String,
        val tpNr: String,
        val orgNr: String
    )
}
