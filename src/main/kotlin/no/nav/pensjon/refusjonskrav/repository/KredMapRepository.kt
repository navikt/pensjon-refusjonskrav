package no.nav.pensjon.refusjonskrav.repository

import no.nav.pensjon.refusjonskrav.domain.KredMap
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kredmap")
class KredMapRepository(
    mappings: Map<String, KredMap>
): Map<String, KredMap> by mappings
