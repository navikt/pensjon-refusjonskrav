package no.nav.pensjon.refusjonskrav.config

import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.RestTemplateMdcInterceptor
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@EnableJwtTokenValidation
class RestConfig(
    @Value("\${sam.url}")
    private val samUrl: String,
    private val azureM2MTokenInterceptor: AzureM2MTokenInterceptor,
    private val restTemplateMdcInterceptor: RestTemplateMdcInterceptor
) {

    @Bean
    fun samRestTemplate(): RestTemplate = RestTemplateBuilder()
        .rootUri(samUrl)
        .additionalInterceptors(
            azureM2MTokenInterceptor,
            restTemplateMdcInterceptor)
        .build()
}
