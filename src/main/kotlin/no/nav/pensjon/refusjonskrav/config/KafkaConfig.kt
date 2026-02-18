package no.nav.pensjon.refusjonskrav.config

import no.nav.pensjon.refusjonskrav.service.kafka.LukkVedtakMelding
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
@Import(KafkaAutoConfiguration::class)
class KafkaConfig {
    @Bean
    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, LukkVedtakMelding> {
        properties.producer.valueSerializer = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(properties.buildProducerProperties())
    }

    @Bean
    fun kafkaTemplate(
        producerFactory: ProducerFactory<String, LukkVedtakMelding>
    ) = KafkaTemplate(producerFactory)
}
