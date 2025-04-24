package no.nav.pensjon.refusjonskrav

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
//@EnableKafka
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
