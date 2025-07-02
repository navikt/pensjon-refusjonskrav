package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.SamClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = "entraID"),
    ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:pensjon/refusjonskrav"])
)
class RefusjonskravController(private val samClient: SamClient) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/refusjonskrav")
    fun opprett(@RequestBody refusjonskrav: Refusjonskrav): ResponseEntity<Unit> {

        logger.debug("Refusjonkrav: {}", refusjonskrav)
        samClient.opprettRefusjonskrav(refusjonskrav)
        return ResponseEntity.noContent().build()

    }

    @GetMapping("/api/ping")
    fun ping(): ResponseEntity<Boolean> = ResponseEntity.ok().body<Boolean>(true)
        .also { logger.info("Ping utført") }

}
