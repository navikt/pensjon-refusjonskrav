package no.nav.pensjon.refusjonskrav.exception

import org.springframework.http.HttpStatus.ACCEPTED
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatusCode
import org.springframework.web.server.ResponseStatusException

sealed class RefusjonskravErrorResponseException(status: HttpStatusCode, message: String): ResponseStatusException(status, message) {
    class OrdningForbiddenException(tpnr: String, orgno: String): RefusjonskravErrorResponseException(FORBIDDEN, "Failed validation. $tpnr not managed by $orgno.")
    class MismatchedPidException: RefusjonskravErrorResponseException(CONFLICT, "Pid i kravet samsvarerer ikke med melding.")
    class MismatchedTpnrException: RefusjonskravErrorResponseException(CONFLICT, "Tpnr i kravet samsvarerer ikke med melding.")
    class MeldingBesvartException: RefusjonskravErrorResponseException(CONFLICT, "Melding besvart eller tidsfrist utløpt.")
    class TrekkStartBeforeVedtakException: RefusjonskravErrorResponseException(CONFLICT, "Refusjonstrekk starter før start av vedtak.")
    class TrekkEndBeforeStartException: RefusjonskravErrorResponseException(CONFLICT, "Refusjonstrekk slutter før det starter.")
    class TrekkEndAfterVedtakException: RefusjonskravErrorResponseException(CONFLICT, "Refusjonstrekk slutter etter avsluttning av vedtak.")
    class FutureTrekkOnRunningVedtakException: RefusjonskravErrorResponseException(CONFLICT, "Refusjonstrekk kan ikke være frem i tid for løpende vedtak.")
    class CouldNotCloseVedtakException: RefusjonskravErrorResponseException(ACCEPTED, "Refusjonskrav registrert, avventer lukking av vedtak.")
}
