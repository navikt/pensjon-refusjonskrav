package no.nav.pensjon.refusjonskrav.repository

import no.nav.pensjon.refusjonskrav.domain.KredMap
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kredmap")
class KredMapRepository(
    private val mappings: Map<String, KredMap>
) {

    operator fun get(tpNr: String): KredMap? = mappings[tpNr]
}
