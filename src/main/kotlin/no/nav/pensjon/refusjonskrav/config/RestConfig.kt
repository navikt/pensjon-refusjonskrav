package no.nav.pensjon.refusjonskrav.config

import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptor
import no.nav.pensjon.refusjonskrav.service.interceptor.AzureM2MTokenInterceptorBuilder
import no.nav.pensjon.refusjonskrav.service.interceptor.RestTemplateMdcInterceptor
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
@EnableJwtTokenValidation
class RestConfig(
    private val restTemplateMdcInterceptor: RestTemplateMdcInterceptor,
    private val azureM2MTokenInterceptorBuilder: AzureM2MTokenInterceptorBuilder
) {

    private fun buildRestTemplate(url: String, tokenInterceptor: AzureM2MTokenInterceptor): RestTemplate =
        RestTemplateBuilder()
            .rootUri(url)
            .additionalInterceptors(tokenInterceptor, restTemplateMdcInterceptor)
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(60))
            .build()

    @Bean
    fun samAzureM2MTokenInterceptor(@Value($$"${sam.scope}") samScope: String) =
        azureM2MTokenInterceptorBuilder.buildForScope(samScope)

    @Bean
    fun samRestTemplate(
        @Value($$"${sam.url}") samUrl: String,
        samAzureM2MTokenInterceptor: AzureM2MTokenInterceptor
    ) = buildRestTemplate(samUrl, samAzureM2MTokenInterceptor)

    @Bean
    fun tpAzureM2MTokenInterceptor(@Value($$"${tp.scope}") tpScope: String) =
        azureM2MTokenInterceptorBuilder.buildForScope(tpScope)

    @Bean
    fun tpRestTemplate(
        @Value($$"${tp.url}") tpUrl: String,
        tpAzureM2MTokenInterceptor: AzureM2MTokenInterceptor
    ) = buildRestTemplate(tpUrl, tpAzureM2MTokenInterceptor)

    @Bean
    fun penAzureM2MTokenInterceptor(@Value($$"${pen.scope}") penScope: String) =
        azureM2MTokenInterceptorBuilder.buildForScope(penScope)

    @Bean
    fun penRestTemplate(
        @Value($$"${pen.url}") penUrl: String,
        penAzureM2MTokenInterceptor: AzureM2MTokenInterceptor
    ) = buildRestTemplate(penUrl, penAzureM2MTokenInterceptor)

    @Bean
    fun osAzureM2MTokenInterceptor(@Value($$"${oppdrag.scope}") osScope: String) =
        azureM2MTokenInterceptorBuilder.buildForScope(osScope)

    @Bean
    fun osRestTemplate(
        @Value($$"${oppdrag.url}") osUrl: String,
        osAzureM2MTokenInterceptor: AzureM2MTokenInterceptor
    ) = buildRestTemplate(osUrl, osAzureM2MTokenInterceptor)
}
