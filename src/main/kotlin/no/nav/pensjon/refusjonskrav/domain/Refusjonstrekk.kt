package no.nav.pensjon.refusjonskrav.domain

import java.time.LocalDateTime

data class Refusjonstrekk(
    var belop: Double,
    val kravstillersRef: String,
    val datoFom: LocalDateTime,
    val datoTom: LocalDateTime,
)
