package no.nav.pensjon.refusjonskrav.domain

import java.time.LocalDate

data class Refusjonstrekk(
    var belop: Double,
    val kravstillersRef: String,
    val datoFom: LocalDate,
    val datoTom: LocalDate
)
