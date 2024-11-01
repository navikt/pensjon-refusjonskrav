package no.nav.pensjon.refusjonskrav.service.interceptor

import no.nav.pensjon.refusjonskrav.config.MdcRequestFilter.Companion.REQUEST_ID_HEADER
import no.nav.pensjon.refusjonskrav.config.MdcRequestFilter.Companion.REQUEST_ID_MDC_KEY
import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component

@Component
class RestTemplateMdcInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers.set(REQUEST_ID_HEADER, MDC.get(REQUEST_ID_MDC_KEY))
        return execution.execute(request, body)
    }
}