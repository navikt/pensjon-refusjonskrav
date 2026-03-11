package no.nav.pensjon.refusjonskrav.config

import com.nimbusds.jwt.JWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.getForObject
import org.springframework.web.server.ResponseStatusException

@Service
class MaskinportenValidator(
    @Value("\${MASKINPORTEN_ISSUER}") private val maskinportenIssuer: String,
    @Value("\${tp.url}") tpUrl: String
) {

    private val restTemplate = RestTemplateBuilder().rootUri(tpUrl).build()
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun validateTpnrAuthorization(tpnr: String, token: JWT) {
        token.jwtClaimsSet.apply {
            if (issuer == maskinportenIssuer) {
                val tokenOrgno = getJSONObjectClaim("consumer")["ID"].toString().substringAfterLast(':')
                try {
                    log.info("Maskinporten token received. Validating tpnr: $tpnr is managed by orgno: $tokenOrgno")
                    doValidation(tpnr, tokenOrgno)
                } catch (e: HttpStatusCodeException) {
                    if (e.statusCode == NOT_FOUND) throw ResponseStatusException(FORBIDDEN, "Failed validation. $tpnr not managed by $tokenOrgno.")
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

    fun doValidation(tpnr: String, orgno: String) = restTemplate.getForObject<Boolean>("/api/tpconfig/organisation/validate/" + tpnr + "_" + orgno)
}
