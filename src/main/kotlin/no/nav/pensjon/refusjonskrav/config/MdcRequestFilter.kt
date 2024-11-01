package no.nav.pensjon.refusjonskrav.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MdcRequestFilter : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest

        val requestId =
            REQUEST_ID_HEADER_CANDIDATES
                .firstOrNull { httpRequest.getHeader(it) != null }
                ?.let { header -> httpRequest.getHeader(header) }
                ?: UUID.randomUUID().toString()

        MDC.put(REQUEST_ID_MDC_KEY, requestId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY)
        }
    }

    companion object {

        const val REQUEST_ID_MDC_KEY = "x_request_id"
        const val REQUEST_ID_HEADER = "X-Request-Id"

        private val REQUEST_ID_HEADER_CANDIDATES = listOf(REQUEST_ID_HEADER)

    }
}