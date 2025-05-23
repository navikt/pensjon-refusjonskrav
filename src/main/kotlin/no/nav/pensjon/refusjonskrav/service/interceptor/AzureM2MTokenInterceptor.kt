package no.nav.pensjon.refusjonskrav.service.interceptor

import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component

@Component
class AzureM2MTokenInterceptor(
    @Value("\${sam.scope}")
    private val samScope: String
    ): ClientHttpRequestInterceptor {

    private val tokenCLient =  AzureAdTokenClientBuilder.builder().withNaisDefaults().buildMachineToMachineTokenClient()

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.setBearerAuth(tokenCLient.createMachineToMachineToken(samScope))
        return execution.execute(request, body)
    }
}