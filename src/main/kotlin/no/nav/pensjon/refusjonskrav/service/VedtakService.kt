package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Fagomrade.AAP
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Fagomrade.EYO
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Fagomrade.PEN
import no.nav.pensjon.refusjonskrav.service.rest.sam.dto.Vedtak
import no.nav.pensjon.refusjonskrav.service.kafka.LukkVedtakMeldingProducer
import no.nav.pensjon.refusjonskrav.service.rest.pen.PenClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VedtakService(
    private val penClient: PenClient,
    private val LukkVedtakMeldingProducer: LukkVedtakMeldingProducer
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun lukkVedtak(vedtak: Vedtak) {
        when(vedtak.fagomrade) {
            PEN -> varslePen(vedtak)
            EYO, AAP -> varsleKafka(vedtak)
        }
    }

    private fun varslePen(vedtak: Vedtak) = penClient.lukkVedtak(vedtak)

    private fun varsleKafka(vedtak: Vedtak) = LukkVedtakMeldingProducer.lukkVedtak(vedtak)
}
