package no.nav.pensjon.refusjonskrav.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.pensjon.refusjonskrav.service.rest.okonomi.OsClient
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import no.nav.pensjon.refusjonskrav.service.rest.sam.SamClient
import no.nav.pensjon.refusjonskrav.service.rest.tp.TpClient
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ServiceAvailabilityFilter(
    val osClient: OsClient,
    val penClient: PenClient,
    val samClient: SamClient,
    val tpClient: TpClient
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        osClient.ping()
        penClient.ping()
        samClient.ping()
        tpClient.ping()
        return true
    }
}
