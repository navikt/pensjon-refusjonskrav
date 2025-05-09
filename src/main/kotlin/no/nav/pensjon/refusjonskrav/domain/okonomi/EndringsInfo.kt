package no.nav.pensjon.refusjonskrav.domain.okonomi

import java.time.LocalDateTime

data class EndringsInfo(
    var kildeId: String
) {
    var opprettetAvId: String? = null
    var opprettetAvNavn: String? = null
    var opprettetAvEnhetId: String? = null
    var opprettetAvEnhetNavn: String? = null
    var opprettetDato: LocalDateTime? = null
    var endretAvId: String? = null
    var endretAvNavn: String? = null
    var endretAvEnhetId: String? = null
    var endretAvEnhetNavn: String? = null
    var endretDato: LocalDateTime? = null
    var kildeNavn: String? = null
}
