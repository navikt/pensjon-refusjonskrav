package no.nav.pensjon.refusjonskrav.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonSetter
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import java.time.LocalDateTime
import java.time.ZoneOffset

@Validated
data class Refusjonstrekk(
    var belop: Double,
    @field:Size(max = 12, message = "kravstillersRef cannot exceed 12 characters.")
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
