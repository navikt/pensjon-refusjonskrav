package no.nav.pensjon.refusjonskrav

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableJwtTokenValidation
//@EnableKafka
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
