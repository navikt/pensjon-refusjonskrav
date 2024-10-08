package no.nav.pensjon.refusjonskrav.controller

import no.nav.pensjon.refusjonskrav.domain.Refusjonskrav
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController(value = "/api/refusjonskrav")
class RefusjonskravController {

    @PostMapping
    fun opprett(
        @RequestBody refusjonskrav: Refusjonskrav
    ) {

    }
}