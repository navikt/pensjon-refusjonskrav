package no.nav.pensjon.refusjonskrav.service

import no.nav.pensjon.refusjonskrav.domain.Fagomrade.EYO
import no.nav.pensjon.refusjonskrav.domain.Fagomrade.PEN
import no.nav.pensjon.refusjonskrav.domain.Melding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MeldingService(
    private val penClient: PenClient,
    private val eyoKafkaProducer: EyoKafkaProducer
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun varsleMelding(melding: Melding) {
        when(melding.vedtak.fagomrade) {
            PEN -> varslePen(melding)
            EYO -> varsleEyo(melding)
        }
    }

        private fun varslePen(melding: Melding) = penClient.varsleRefusjonskrav(melding)

        private fun varsleEyo(melding: Melding) = eyoKafkaProducer.varsleRefusjonskrav(melding)
}
