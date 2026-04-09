package no.nav.pensjon.refusjonskrav.service.rest.sam.dto

data class OpprettHendelseRequest(
    val fnr: String,
    val tpnr: String,
    val hendelseType: HendelseType = HendelseType.REFUSJONSKRAV,
    val kanalType: KanalType = KanalType.WEB_SERVICE
)
