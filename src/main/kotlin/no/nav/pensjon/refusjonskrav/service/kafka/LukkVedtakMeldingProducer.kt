package no.nav.pensjon.refusjonskrav.service.kafka

import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class LukkVedtakMeldingProducer(
    @Value($$"${vedtak.samhandlersvar.topic:test-topic}")
    private val topic: String,
    private val kafkaTemplate: KafkaTemplate<String, LukkVedtakMelding>
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun lukkVedtak(vedtak: Vedtak) {
        try {
            logger.info("Closing vedtak: ${vedtak.samVedtakId}.")
            kafkaTemplate.send(topic, LukkVedtakMelding(vedtak)).get()
        } catch (e: Exception) {
            logger.error("Failed to close vedtak: ${vedtak.samVedtakId}.", e)
            throw e
        }
    }
}
