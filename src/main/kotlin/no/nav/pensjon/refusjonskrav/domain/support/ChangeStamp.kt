package no.nav.pensjon.refusjonskrav.domain.support

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.PreUpdate
import java.time.LocalDateTime

@Embeddable
class ChangeStamp(creator: String) {
    @Column(name = "OPPRETTET_AV", updatable = false)
    val createdBy: String = creator

    @Column(name = "DATO_OPPRETTET", updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now()

    @Column(name = "ENDRET_AV")
    var updatedBy: String = createdBy
        private set

    @Column(name = "DATO_ENDRET")
    var updatedDate: LocalDateTime = createdDate
        private set

    @PreUpdate
    fun updatedBy() {
        updatedBy = TODO("Get user name from security context")
        updatedDate = LocalDateTime.now()
    }
}
