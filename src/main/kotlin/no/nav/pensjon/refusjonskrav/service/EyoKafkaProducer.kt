package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Melding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EyoKafkaProducer {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun varsleRefusjonskrav(melding: Melding) {
        TODO("Varsle EYO (Kafka)")
    }
}
