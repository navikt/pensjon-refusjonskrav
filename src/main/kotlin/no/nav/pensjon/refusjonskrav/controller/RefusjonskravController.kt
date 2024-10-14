package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import no.nav.pensjon.refusjonskrav.service.SamClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/api/refusjonskrav")
class RefusjonskravController(
    private val samClient: SamClient
) {

    @PostMapping
    fun opprett(
        @RequestBody refusjonskrav: Refusjonskrav
    ): ResponseEntity<Boolean> {
        return ResponseEntity.ok().body(samClient.opprettRefusjonskrav(refusjonskrav))
    }
}