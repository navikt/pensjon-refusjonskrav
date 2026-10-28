# pensjon-refusjonskrav
Behandling av refusjonskrav fra tjenestepensjonsordningene

## Teknisk grunnlag

Java 21, Spring Boot 4.1.1, token-support 6.0.14 og Gradle-wrapper 8.14.4.
Jackson 3.2.4 brukes til applikasjonens JSON. Logstash-encoderen trenger fortsatt
Jackson 2.21.8; begge bruker jackson-annotations 2.22.
Videre vedlikehold av token-support 6.x er ikke avklart. Versjon 7.x krever Java 25.

## Verifikasjon

```shell
./gradlew build
```

Testene dekker gyldige og ugyldige tokens, Maskinporten/TP, SAMs JSON med
epoch-millis og feilsvar, loggmaskering, health/prometheus og lokal M2M-integrasjon.
De bruker ikke produksjonsintegrasjonene.

## Utrulling og tilbakeføring

Rull ut i dev først. Kontroller health/prometheus, testkall med autorisert og avvist
token, feilhåndtering mot SAM/TP og loggmaskering. Ikke logg personopplysninger.
Ved feil: rull tilbake til forrige applikasjonsimage. Ingen databasemigrasjon inngår.
