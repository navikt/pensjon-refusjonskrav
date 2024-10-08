package no.nav.pensjon.refusjonskrav.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SamClient(
    private val samRestTemplate: RestTemplate
) {

}