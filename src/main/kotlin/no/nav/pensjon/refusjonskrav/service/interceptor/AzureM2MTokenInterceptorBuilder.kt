package no.nav.pensjon.refusjonskrav.service.interceptor

import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import org.springframework.stereotype.Component

@Component
class AzureM2MTokenInterceptorBuilder {

    private val tokenCLient =  AzureAdTokenClientBuilder.builder().withNaisDefaults().buildMachineToMachineTokenClient()

    fun buildForScope(scope: String) = AzureM2MTokenInterceptor(scope, tokenCLient)
}
