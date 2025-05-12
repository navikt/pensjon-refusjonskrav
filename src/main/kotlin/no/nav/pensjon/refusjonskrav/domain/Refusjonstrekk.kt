package no.nav.pensjon.refusjonskrav.domain

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class Refusjonstrekk(
    var belop: Double,
    val kravstillersRef: String,
    @get:JsonFormat(shape = JsonFormat.Shape.NUMBER)
    val datoFom: LocalDateTime,
    @get:JsonFormat(shape = JsonFormat.Shape.NUMBER)
    val datoTom: LocalDateTime,
)
