package no.nav.pensjon.refusjonskrav.config

import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kafka.annotation.EnableKafka

@Configuration
@EnableKafka
@Import(KafkaAutoConfiguration::class)
class KafkaConfig {
//    @Bean
//    fun producerFactory(properties: KafkaProperties): ProducerFactory<String, LukkVedtakMelding> {
//        properties.producer.valueSerializer = JsonSerializer::class.java
//        return DefaultKafkaProducerFactory(properties.buildProducerProperties())
//    }
//
//    @Bean
//    fun kafkaTemplate(
//        producerFactory: ProducerFactory<String, LukkVedtakMelding>
//    ) = KafkaTemplate(producerFactory)
}
