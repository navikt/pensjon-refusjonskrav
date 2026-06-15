package no.nav.pensjon.refusjonskrav.controller

import com.nimbusds.jwt.JWTParser
import jakarta.validation.Valid
import no.nav.pensjon.refusjonskrav.config.MaskinportenValidator
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.SamClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = "entraID"),
    ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:pensjon/refusjonskrav"])
)
class RefusjonskravController(
    private val samClient: SamClient,
    private val maskinportenValidator: MaskinportenValidator
) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @Validated
    @PostMapping("/api/refusjonskrav")
    fun opprett(
        @Valid @RequestBody refusjonskrav: Refusjonskrav,
        @RequestHeader(name = "Authorization") bearerToken: String
    ): ResponseEntity<Unit> {
        maskinportenValidator.validateTpnrAuthorization(
            refusjonskrav.tpNr,
            JWTParser.parse(bearerToken.removePrefix("Bearer "))
        )
        logger.debug("Refusjonkrav: {}", refusjonskrav)
        samClient.opprettRefusjonskrav(refusjonskrav)
        return ResponseEntity.noContent().build()

    }

    @GetMapping("/api/ping")
    fun ping(): ResponseEntity<Boolean> = ResponseEntity.ok().body<Boolean>(true)
        .also { logger.info("Ping utført") }
}
