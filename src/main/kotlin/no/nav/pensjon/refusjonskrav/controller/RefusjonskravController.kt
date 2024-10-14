package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.SamClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RefusjonskravController(private val samClient: SamClient) {

    private val logger : Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/refusjonskrav/")
    fun opprett(@RequestBody refusjonskrav: Refusjonskrav): ResponseEntity<Boolean> {

        logger.debug("Refusjonkrav: $refusjonskrav")

        return ResponseEntity.ok().body(samClient.opprettRefusjonskrav(refusjonskrav))

    }
}