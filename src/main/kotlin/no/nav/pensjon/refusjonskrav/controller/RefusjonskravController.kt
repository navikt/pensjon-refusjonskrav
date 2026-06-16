package no.nav.pensjon.refusjonskrav.controller

import jakarta.validation.Valid
import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.RefusjonskravService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequiredIssuers(
    ProtectedWithClaims(issuer = "entraID"),
    ProtectedWithClaims(issuer = "maskinporten", claimMap = ["scope=nav:pensjon/refusjonskrav"])
)
class RefusjonskravController(
    private val refusjonskravService: RefusjonskravService
) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @Validated
    @PostMapping("/api/refusjonskrav")
    @ResponseStatus(NO_CONTENT)
    fun opprett(
        @Valid @RequestBody refusjonskrav: Refusjonskrav,
        @RequestAttribute(required = false) orgno: String?
    ) {
        refusjonskravService.behandleRefusjonskrav(refusjonskrav, orgno)
    }

    @GetMapping("/api/ping")
    fun ping(): Boolean {
        logger.info("Ping utført")
        return true
    }
}
