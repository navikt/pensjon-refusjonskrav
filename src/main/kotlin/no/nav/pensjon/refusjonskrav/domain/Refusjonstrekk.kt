package no.nav.pensjon.refusjonskrav.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonSetter
import java.time.LocalDateTime
import java.time.ZoneOffset

data class Refusjonstrekk(
    var belop: Double,
    val kravstillersRef: String,
    @JsonSetter
    val datoFom: LocalDateTime,
    @JsonSetter
    val datoTom: LocalDateTime,
) {
    @JsonGetter("datoFom")
    fun datoFomMillis() = datoFom.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()

    @JsonGetter("datoTom")
    fun datoTomMillis() = datoTom.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
}
