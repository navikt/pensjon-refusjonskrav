package no.nav.pensjon.refusjonskrav.config

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.HandlerInterceptor

@Service
class MaskinportenOrgnoInterceptor(
    @Value($$"${MASKINPORTEN_ISSUER}") private val maskinportenIssuer: String,
) : HandlerInterceptor {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        setOrgnoRequestAttribute(request.token?.jwtClaimsSet?.orgno)
        return true
    }

    private fun setOrgnoRequestAttribute(orgno: String?) {
        if (orgno != null) {
            RequestContextHolder.currentRequestAttributes().setAttribute("orgno", orgno, SCOPE_REQUEST)
        }
    }

    val HttpServletRequest.token
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
