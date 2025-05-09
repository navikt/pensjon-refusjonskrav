package no.nav.pensjon.refusjonskrav.domain.okonomi

import no.nav.pensjon.refusjonskrav.domain.Refusjonstrekk
import no.nav.pensjon.refusjonskrav.domain.TPKredMap
import java.time.LocalDate
import java.util.Date

data class AndreTrekk(
    var debitorOffnr: String,
    var trekktypeKode: String,
    var trekkperiodeFom: LocalDate,
    var trekkperiodeTom: LocalDate,
    var tssEksternId: String,
    var trekkAlternativKode: KOppdragssystemCodes,
    var sats: String,
    var kreditorRef: String,
    var prioritetFom: LocalDate,
    var endringsInfo: EndringsInfo,
    var fagomradeListe: Set<Fagomrade>,
) {
    var trekkvedtakId: String? = null
    var debitorNavn: String? = null
    var trekktypeBeskrivelse: String? = null
    var trekkstatusKode: String? = null
    var trekkstatusBeskrivelse: String? = null
    var kreditorOffnr: String? = null
    var kreditorAvdelingsnr: String? = null
    var kreditorNavn: String? = null
    var kreditorKid: String? = null
    var prioritet: String? = null
    var trekkAlternativBeskrivelse: String? = null
    var belopSaldotrekk: String? = null
    var belopTrukket: String? = null
    var datoOppfolging: Date? = null
    var maksbelop: Maksbelop? = null

    constructor(
        pid: String,
        tssEksternId: String,
        prioritetFom: LocalDate,
        tpKredCodes: TPKredMap,
        refusjonstrekk: Refusjonstrekk
    ) : this(
        debitorOffnr = pid,
        tssEksternId = tssEksternId,
        trekktypeKode = tpKredCodes.trekkType,
        sats = refusjonstrekk.belop.toString(),
        kreditorRef = refusjonstrekk.kravstillersRef,
        trekkperiodeFom = refusjonstrekk.datoFom,
        trekkperiodeTom = refusjonstrekk.datoTom,
        prioritetFom = prioritetFom,
        trekkAlternativKode = KOppdragssystemCodes.LOPM,
        endringsInfo = EndringsInfo(
            kildeId = tssEksternId
        ),
        fagomradeListe = setOf(
            Fagomrade(
                trekkgruppeKode = tpKredCodes.trekkGruppe,
            )
        ))
}
