package no.nav.pensjon.refusjonskrav.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.ui.ModelMap
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.WebRequest
import org.springframework.web.context.request.WebRequestInterceptor

@Service
class MaskinportenOrgnoInterceptor(
    @Value("\${MASKINPORTEN_ISSUER}") private val maskinportenIssuer: String,
) : WebRequestInterceptor {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: WebRequest) {
        setOrgnoRequestAttribute(request.token?.jwtClaimsSet?.orgno)
    }

    override fun postHandle(
        request: WebRequest,
        model: ModelMap?
    ) {
    }

    override fun afterCompletion(
        request: WebRequest,
        ex: Exception?
    ) {
    }

    private fun setOrgnoRequestAttribute(orgno: String?) {
        RequestContextHolder.currentRequestAttributes().setAttribute("orgno", orgno, SCOPE_REQUEST)
    }

    val WebRequest.token
        get() = getHeader("Authorization")?.substringAfter("Bearer ")?.asJwt

    val String.asJwt: SignedJWT
        get() = SignedJWT.parse(this)

    val JWTClaimsSet.orgno
        get() = if (issuer == maskinportenIssuer) {
            getJSONObjectClaim("consumer")["ID"].toString().substringAfterLast(':').also {
                log.info("Reived request using Maskinporten with orgno $it.")
            }
        } else null
}
