package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

data class FindSamordningMeldingRequest(val pid: String, val samId: Long, val tpNr: String)
