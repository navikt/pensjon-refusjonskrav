package no.nav.pensjon.refusjonskrav.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import org.springframework.stereotype.Component

@Component
class ServiceAvailabilityFilter(
    val osClient: OsClient,
    val penClient: PenClient,
    val samClient: SamClient,
    val tpClient: TpClient
) : Filter {

    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        osClient.ping()
        penClient.ping()
        samClient.ping()
        tpClient.ping()
        filterChain.doFilter(request, response)
    }
}
