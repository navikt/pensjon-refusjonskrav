package no.nav.pensjon.refusjonskrav.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestConfig(
    @Value("\${sam.url}")
    private val samUrl: String
) {

    @Bean
    fun samRestTemplate(): RestTemplate = RestTemplateBuilder().rootUri(samUrl) .build()
}