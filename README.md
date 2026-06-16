# pensjon-refusjonskrav

Behandling av refusjonskrav fra tjenestepensjonsordningene.

Applikasjonen mottar og behandler refusjonskrav fra eksterne tjenestepensjonsordninger (f.eks. SPK, KLP, Storebrand).
Kravene knyttes til vedtak i SAM-systemet, valideres, og resulterer i oppretting av trekk i oppdragssystemet (OS).
Når alle meldinger tilknyttet et vedtak er besvart, lukkes vedtaket og relevante systemer varsles.

---

## Innhold

- [Funksjonalitet](#funksjonalitet)
- [Arkitektur og integrasjoner](#arkitektur-og-integrasjoner)
- [API](#api)
- [Autentisering](#autentisering)
- [Konsumenter (Maskinporten)](#konsumenter-maskinporten)
- [Teknologi](#teknologi)
- [Miljøer](#miljøer)
- [Kom i gang](#kom-i-gang)

---

## Funksjonalitet

1. **Mottak av refusjonskrav** – ekstern pensjonsleverandør sender inn et krav med SAM-meldings-ID og en liste over periodiserte beløp (refusjonstrekk).
2. **Validering** – kravets perioder og beløp valideres mot vedtaket hentet fra SAM.
3. **Oppretting av trekk** – gyldige krav resulterer i «andre trekk» i oppdragssystemet via OS-klienten.
4. **Oppdatering av SAM** – meldingen og vedtaket markeres som besvart i SAM.
5. **Lukking av vedtak** – når alle meldinger på et vedtak er besvart, lukkes vedtaket:
   - **PEN**-fagområde: REST-kall til PEN.
   - **EYO / AAP**-fagområde: melding sendes på Kafka.

---

## Arkitektur og integrasjoner

```
Ekstern pensjonsleverandør
        │  POST /api/refusjonskrav
        ▼
pensjon-refusjonskrav
   ├── SAM       – henter/oppdaterer meldinger og vedtak
   ├── TP        – validerer tpnr og henter ytelser / TSS-ekstern-id
   ├── OS        – oppretter «andre trekk» (sokos-oppdrag-proxy)
   ├── PEN       – lukker vedtak for PEN-fagområde
   └── Kafka     – lukker vedtak for EYO/AAP-fagområde
```

---

## API

| Metode | Sti | Beskrivelse |
|--------|-----|-------------|
| `POST` | `/api/refusjonskrav` | Send inn et refusjonskrav |
| `GET`  | `/api/ping` | Enkel ping/helsesjekk |

### Eksempel – request body

```json
{
  "samId": 123456,
  "refusjonskrav": true,
  "periodisertBelopListe": [
    {
      "belop": 1500.00,
      "kravstillersRef": "REF-2024-001",
      "datoFom": 1704067200000,
      "datoTom": 1706745600000
    }
  ]
}
```

Respons: `204 No Content` ved suksess.

---

## Autentisering

Endepunktet `/api/refusjonskrav` krever ett av følgende tokens:

| Metode | Brukes av |
|--------|-----------|
| **Maskinporten** – scope `nav:pensjon/refusjonskrav` | Eksterne pensjonsleverandører |
| **EntraID** (Azure AD) | Interne NAV-systemer |

---

## Konsumenter (Maskinporten)

Følgende organisasjoner er autorisert til å sende refusjonskrav:

| Navn | Orgnr |
|------|-------|
| SPK | 982583462 |
| KLP | 938708606 |
| Storebrand Livsforsikring AS | 958995369 |
| Storebrand Pensjonstjenester AS | 931936492 |
| GABLER PENSJONSTJENESTER AS | 916833520 |
| Oslo Pensjonsforsikring AS | 982759412 |
| Viken | 974371359 |
| Arendal kommunale pensjonskasse | 940380014 |
| Drammen kommunale pensjonskasse | 980650383 |
| Pensjonsordningen for apotekvirksomhet | 940291380 |
| Maritim pensjonskasse | 940415683 |
| Garantikassen for fiskere | 974652382 |
| NAV | 889640782 |

---

## Teknologi

- **Kotlin** / **Spring Boot 4**
- **JVM 21**
- **Kafka** (lukking av vedtak for EYO/AAP)
- **Maskinporten** og **EntraID** for autentisering
- **Prometheus** / **OpenTelemetry** for metrikker og sporing
- Kjøres på **NAIS** (GCP) i namespace `pensjonsamhandling`

---

## Miljøer

| Miljø | URL |
|-------|-----|
| Dev (intern) | https://pensjon-refusjonskrav.intern.dev.nav.no |
| Dev (ekstern) | https://pensjon-refusjonskrav.ekstern.dev.nav.no |
| Prod (intern) | https://pensjon-refusjonskrav.intern.nav.no |
| Prod (ekstern) | https://pensjon-refusjonskrav.ekstern.nav.no |

---

## Kom i gang

### Forutsetninger

- JDK 21
- Et gyldig `GITHUB_TOKEN` med tilgang til `navikt/maskinporten-validation`-pakken

### Bygg og kjør tester

```bash
./gradlew build
```

### Kjør lokalt

```bash
./gradlew bootRun
```

Applikasjonen starter på port `8080`.

Er det noe en lurer på kontakt oss internt i nav på slack
#samhandling-utviklere