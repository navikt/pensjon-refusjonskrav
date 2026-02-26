package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

import java.time.LocalDate

data class UpdateMeldingRequest(
    val refusjonskrav: Boolean,
    val datoSvart: LocalDate,
    val meldingStatus: MeldingStatus
)
