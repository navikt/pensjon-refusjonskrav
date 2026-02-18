package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Melding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PenClient(
    private val penRestTemplate: RestTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        TODO("Ping PEN.")
    }

    fun varsleRefusjonskrav(melding: Melding) {
        TODO("Varsle PEN (REST)")
    }
}
