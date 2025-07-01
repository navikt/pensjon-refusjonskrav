package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.controller.TokenHelper.Issuer.*
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.web.server.ResponseStatusException

internal class TokenHelperTest {

    private val encodedToken = javaClass.getResource ("/token/mockEncodedToken.txt")?.readText() ?: throw RuntimeException("Ingen testtoken funnet")
    private val encodedSrvToken = javaClass.getResource("/token/mockEncodedSrvUsrToken.txt")?.readText() ?: throw RuntimeException("Ingen testtoken funnet")
    private val encodedMaskinToken = javaClass.getResource("/token/maskinporten-navtoken.txt")?.readText() ?: throw RuntimeException("Ingen testtoken funnet")

    @Nested
    @DisplayName("Check for tokens")
    inner class ValidTokens {

        private val jwt = JwtToken(encodedToken)
        private val jwtsrv = JwtToken(encodedSrvToken)
        private val jwtmsk = JwtToken(encodedMaskinToken)
        private val context = TokenValidationContext(mapOf(TOKENDINGS.name.lowercase() to jwt, DIFI.name.lowercase() to jwt, SERVICEBRUKER.name.lowercase() to jwtsrv, MASKINPORTEN.name.lowercase() to jwtmsk))
        private val tokenContext = TokenContext(context)
        private val tokenhelper = TokenHelper(tokenContext)

        @Test
        fun getPidFromTokenX() {
            assertEquals("12345678901", tokenhelper.getPidFromToken())
        }

        @Test
        fun getPid() {
            assertEquals("12345678901", tokenhelper.getPid())
        }

        @Test
        fun getSystemUser() {
            assertEquals ("srvsporingslogg", tokenhelper.getSystemUserId())
        }

        @Nested
        @DisplayName("Maskinporten token test")
        inner class MaskinportenTest {

            @Test
            fun getOrgnrFromMaskinporten() {
                assertEquals("889640782", tokenhelper.getMaskinportenOrg())
            }

        }

    }


    @Nested
    @DisplayName("No valid tokens")
    inner class NotValidTokens {

//        private val encodedToken = javaClass.getResource("/mockEncodedToken.txt")?.readText() ?: RuntimeException("Ingen testtoken funnet")
        private val jwt = JwtToken(encodedToken)
        private val context = TokenValidationContext(mapOf("tokend" to jwt, "Syserr" to jwt))
        private val tokenContext = TokenContext(context)
        private val tokenhelper = TokenHelper(tokenContext)

        @Test
        fun getPidFromTokenX() {
            println("jwt: " + jwt.jwtTokenClaims)
            assertThrows<ResponseStatusException> {
                tokenhelper.getPidFromToken()
            }
        }

        @Test
        fun getPid() {
            assertThrows<ResponseStatusException> {
                tokenhelper.getPid()
            }
        }

        @Test
        fun getSystemUserThrowError() {
            assertThrows<ResponseStatusException> {
                tokenhelper.getSystemUserId()
            }
        }


    }



    private class TokenContext(private val context: TokenValidationContext): TokenValidationContextHolder {
        override fun getTokenValidationContext(): TokenValidationContext {
            return context
        }

        override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
            //not in use
        }
    }



}