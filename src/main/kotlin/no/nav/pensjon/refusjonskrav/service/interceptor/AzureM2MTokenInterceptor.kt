package no.nav.pensjon.refusjonskrav.service.interceptor

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody

@Component
class AzureM2MTokenInterceptor(
    @param:Value("\${sam.scope}")
    private val samScope: String,
    @param:Value($$"${NAIS_TOKEN_ENDPOINT}")
    private val naisTokenEndpoint: String,
) : ClientHttpRequestInterceptor {

    private val restCLient = RestClient.builder().baseUrl(naisTokenEndpoint).build()
    private val accessToken
        get() = restCLient.post().body(texasPayload).retrieve()
            .requiredBody<TexasTokenResponse>().accessToken

    data class TexasTokenResponse(
        @param:JsonProperty("access_token")
        val accessToken: String,
    )

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        request.headers.setBearerAuth(accessToken)
        return execution.execute(request, body)
    }

    private val texasPayload = object {
        @JsonProperty("identity_provider")
        val identityProvider = "entra_id"
        val target = samScope
    }
}