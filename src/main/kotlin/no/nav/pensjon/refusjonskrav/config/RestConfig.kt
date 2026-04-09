package no.nav.pensjon.refusjonskrav.config

import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptorBuilder
import no.nav.pensjon.refusjonskrav.service.interceptor.RestTemplateMdcInterceptor
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableJwtTokenValidation
class RestConfig {

    @Bean
    fun samAzureM2MTokenInterceptor(
        @Value("\${sam.scope}")
        samScope: String,
        azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder
    ) = azureM2MTokenInterceptorBuilder.buildForScope(samScope)

    @Bean
    fun samRestTemplate(
        @Value("\${sam.url}")
        samUrl: String,
        samAzureM2MTokenInterceptor: AzureM2MTokenInterceptor,
        restTemplateMdcInterceptor: RestTemplateMdcInterceptor
    ): RestTemplate = RestTemplateBuilder()
        .rootUri(samUrl)
        .additionalInterceptors(
            samAzureM2MTokenInterceptor,
            restTemplateMdcInterceptor
        )
        .connectTimeout(Duration.ofSeconds(60))
        .readTimeout(Duration.ofSeconds(60))
        .build()

    @Bean
    fun tpAzureM2MTokenInterceptor(
        @Value("\${tp.scope}")
        tpScope: String,
        azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder
    ) = azureM2MTokenInterceptorBuilder.buildForScope(tpScope)

    @Bean
    fun tpRestTemplate(
        @Value("\${tp.url}")
        tpUrl: String,
        tpAzureM2MTokenInterceptor: AzureM2MTokenInterceptor,
        restTemplateMdcInterceptor: RestTemplateMdcInterceptor
    ): RestTemplate = RestTemplateBuilder()
        .rootUri(tpUrl)
        .additionalInterceptors(
            tpAzureM2MTokenInterceptor,
            restTemplateMdcInterceptor
        )
        .build()

    @Bean
    fun penAzureM2MTokenInterceptor(
        @Value("\${pen.scope}")
        penScope: String,
        azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder
    ) = azureM2MTokenInterceptorBuilder.buildForScope(penScope)

    @Bean
    fun penRestTemplate(
        @Value("\${pen.url}")
        penUrl: String,
        penAzureM2MTokenInterceptor: AzureM2MTokenInterceptor,
        restTemplateMdcInterceptor: RestTemplateMdcInterceptor
    ): RestTemplate = RestTemplateBuilder()
        .rootUri(penUrl)
        .additionalInterceptors(
            penAzureM2MTokenInterceptor,
            restTemplateMdcInterceptor
        )
        .build()

    @Bean
    fun osAzureM2MTokenInterceptor(
        @Value("\${oppdrag.scope}")
        osScope: String,
        azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder
    ) = azureM2MTokenInterceptorBuilder.buildForScope(osScope)

    @Bean fun osRestTemplate(
        @Value("\${oppdrag.url}")
        osUrl: String,
        osAzureM2MTokenInterceptor: AzureM2MTokenInterceptor,
        restTemplateMdcInterceptor: RestTemplateMdcInterceptor
    ): RestTemplate = RestTemplateBuilder()
        .rootUri(osUrl)
        .additionalInterceptors(
            osAzureM2MTokenInterceptor,
            restTemplateMdcInterceptor
        ).build()
}
