package no.nav.pensjon.refusjonskrav.service.rest.tp.dto

import java.time.LocalDate

class Ytelse(
    var innmeldtFom: LocalDate,
    var ytelseKode: String
)
