package no.nav.pensjon.refusjonskrav.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val serviceAvailabilityFilter: ServiceAvailabilityFilter,
    private val maskinportenOrgnoInterceptor: MaskinportenOrgnoInterceptor
): WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(serviceAvailabilityFilter)
        registry.addInterceptor(maskinportenOrgnoInterceptor)
    }

}
