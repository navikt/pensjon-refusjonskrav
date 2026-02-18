package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.okonomi.OpprettAndreTrekkRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OsClient {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun ping() {
        TODO("Ping OS.")
    }

    fun opprettAndreTrekk(request: OpprettAndreTrekkRequest) {
        TODO("Send request to OS")
    }
}
