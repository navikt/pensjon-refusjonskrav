package no.nav.pensjon.refusjonskrav.service.rest.pen

import no.nav.pensjon.refusjonskrav.domain.Vedtak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

@Service
class PenClient(
    private val penRestTemplate: RestTemplate,
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        penRestTemplate.getForObject<String>("/ping")
    }

    fun lukkVedtak(vedtak: Vedtak) {
        penRestTemplate.postForObject<Unit>("/vedtak/${vedtak.fagVedtakId}/mottaSamhandlerSvar")
    }
}
