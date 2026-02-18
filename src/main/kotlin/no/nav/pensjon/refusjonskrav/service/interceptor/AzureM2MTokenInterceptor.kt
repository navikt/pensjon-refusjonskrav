package no.nav.pensjon.refusjonskrav.service.interceptor

import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class AzureM2MTokenInterceptor(
    private val scope: String,
    private val tokenClient: AzureAdMachineToMachineTokenClient
): ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.setBearerAuth(tokenClient.createMachineToMachineToken(scope))
        return execution.execute(request, body)
    }
}
