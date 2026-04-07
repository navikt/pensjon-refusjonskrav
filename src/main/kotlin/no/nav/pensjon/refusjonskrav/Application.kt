package no.nav.pensjon.refusjonskrav

import no.nav.pensjon.refusjonskrav.repository.KredMapRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(KredMapRepository::class)
//@EnableKafka
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
