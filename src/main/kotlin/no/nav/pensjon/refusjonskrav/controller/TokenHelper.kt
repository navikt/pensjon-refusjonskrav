package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.controller.TokenHelper.Issuer.*
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TokenHelper(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    private val log = LoggerFactory.getLogger(javaClass)

    // se appliation.yam #no.nav.security.jwt. under issuer.xxxx
    enum class Issuer {
        DIFI,
        SERVICEBRUKER,
        TOKENDINGS,
        MASKINPORTEN;
        fun lowercase(): String = this.name.lowercase()
    }

    private fun getClaims(issuer: Issuer): String {
        val context = tokenValidationContextHolder.getTokenValidationContext()
        if(context.issuers.isEmpty())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No issuer found in context")

        val optinalIssuer = context.getJwtTokenAsOptional(issuer.lowercase())

        val jwtToken = if (optinalIssuer.isPresent) {
            optinalIssuer.get()
        } else {
            log.error("No valid token found for issuer: $issuer")
            throw ResponseStatusException(HttpStatus.NOT_FOUND ,"No valid token found")
       }

       return when (issuer) {
           DIFI ->    jwtToken.jwtTokenClaims.get("pid").toString()
           else -> jwtToken.subject
       }
    }

    private fun getMaskinportenOrgnr(jwtTokenclaims: JwtTokenClaims) : String {
        val consumerStr = jwtTokenclaims.get("consumer").toString()
            .replace("{","").replace("}","")
        return consumerStr.substringAfter("0192:")
    }

    /**
     * delevis lånt fra https://github.com/navikt/pam-samtykke-api
     */
    private fun extractForTokendingsIssuer(issuer: Issuer): String {
        val context = tokenValidationContextHolder.getTokenValidationContext()

        try {
            val tokenclaims = context.getClaims(issuer.lowercase())
            return when (issuer) {
                MASKINPORTEN -> getMaskinportenOrgnr(tokenclaims)
                else -> tokenclaims.get("pid").toString()
            }

        } catch (ex: IllegalArgumentException) {
            log.warn("faild to find pid on $issuer", ex)
            try {
                val tokenclaims = context.getClaims(issuer.lowercase())
                return tokenclaims.subject
            } catch (ex: IllegalArgumentException) {
                log.error("No valid token found for issuer: tokendings")
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "No valid token found")
            }
        }
    }

    fun getMaskinportenOrg(): String = extractForTokendingsIssuer(MASKINPORTEN)

    fun getPid(): String = getClaims(DIFI)

    fun getSystemUserId(): String = getClaims(Issuer.SERVICEBRUKER)

    fun getPidFromToken(): String = extractForTokendingsIssuer(TOKENDINGS)

}