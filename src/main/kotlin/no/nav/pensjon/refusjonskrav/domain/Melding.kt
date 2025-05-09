package no.nav.pensjon.refusjonskrav.domain

import java.time.LocalDate

class Melding(
    val samId: Long,
    val vedtak: Vedtak,
    val kanal: Kanal,
    val datoSendt: LocalDate?,
    var datoSvart: LocalDate? = null,
    val datoPurret: LocalDate? = null,
    val tssEksternId: String,
    var refusjonskrav: Boolean? = null,
    var meldingStatus: MeldingStatus,
    val antallForsok: Int
)
