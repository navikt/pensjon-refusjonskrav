package no.nav.pensjon.refusjonskrav.repository

import no.nav.pensjon.refusjonskrav.domain.ArtType
import no.nav.pensjon.refusjonskrav.domain.TPKredMap
import org.springframework.data.jpa.repository.JpaRepository

interface TPKredMapRepository: JpaRepository<TPKredMap, Long> {
    fun findByTssEksternIdFkAndUnderArt(tssEksternId: String, underArt: ArtType): TPKredMap?
}
