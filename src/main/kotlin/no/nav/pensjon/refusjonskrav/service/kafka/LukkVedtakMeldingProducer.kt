package no.nav.pensjon.refusjonskrav.service.kafka

import no.nav.pensjon.refusjonskrav.domain.Vedtak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class LukkVedtakMeldingProducer(
    @Value("\${vedtak.samhandlersvar.topic:test-topic}")
    private val topic: String,
    private val kafkaTemplate: KafkaTemplate<String, LukkVedtakMelding>
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun lukkVedtak(vedtak: Vedtak) {
        kafkaTemplate.send(topic, LukkVedtakMelding(vedtak))
    }
}
