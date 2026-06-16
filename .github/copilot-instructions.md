# Copilot Instructions

## Build & Test

```bash
# Build
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "no.nav.pensjon.refusjonskrav.service.RefusjonskravServiceTest"

# Run a single test method
./gradlew test --tests "no.nav.pensjon.refusjonskrav.service.RefusjonskravServiceTest.Behandle refusjonkrav med standard trekktype"
```

Requires `GITHUB_TOKEN` env var to resolve `maskinporten-validation` from GitHub Packages.

## Architecture

This is a Spring Boot + Kotlin service (JVM 21) that processes **refusjonskrav** (reimbursement claims) submitted by external pension service providers (tjenestepensjonsordningene) to NAV.

**Single endpoint:** `POST /api/refusjonskrav` — accepts either EntraID or Maskinporten tokens.

**Request flow:**
1. `RefusjonskravController` receives the request and passes an optional `orgno` (extracted from Maskinporten token by `MaskinportenOrgnoInterceptor`)
2. `ServiceAvailabilityFilter` pings all four upstream services on every request before the handler runs
3. `RefusjonskravService` orchestrates:
   - Fetch melding from **SAM** (samordning)
   - Validate claim fields and date ranges
   - Register svar (answer) via SAM
   - Create `AndreTrekk` (deduction orders) via **OS** (Oppdragssystemet / sokos-oppdrag-proxy)
   - Close vedtak via **PEN** (REST) for `PEN` fagområde, or via **Kafka** for `EYO`/`AAP`

**Downstream clients** (all using Azure M2M tokens):
- `SamClient` — samordning meldinger/vedtak
- `TpClient` — tjenestepensjonsforhold, ytelser, tpnr validation
- `PenClient` — close vedtak for PEN fagområde
- `OsClient` — create `AndreTrekk` orders

Each client has a dedicated `RestTemplate` bean with its own `AzureM2MTokenInterceptor`, all wired in `RestConfig`.

**Kreditor mapping:** `KredMapRepository` loads a static `tpNr → KredMap` mapping from `kredmap.yaml` (imported via `spring.config.import`) using `@ConfigurationProperties(prefix = "kredmap")`.

## Key Conventions

**Domain language is Norwegian:** `melding`, `vedtak`, `tpNr`, `samId`, `refusjonstrekk`, `fagomrade`, `underArt`, etc. Keep this naming in new code.

**Error handling:** All business-logic errors extend `RefusjonskravErrorResponseException` (a sealed class of `ResponseStatusException` subclasses). Add new error cases there, not as generic exceptions.

**Testing pattern:**
- Tests use `@SpringBootTest` + `@EnableWireMock` to stub HTTP calls to upstreams
- Use `@MockkBean` / `@MockkSpyBean` (springmockk) — not `@MockBean`
- Auth is mocked with `@EnableMockOAuth2Server` (mock-oauth2-server)
- `AzureM2MTokenInterceptor` beans must be mocked in every `@SpringBootTest` to bypass token acquisition; pass-through via `(args[2] as ClientHttpRequestExecution).execute(...)` pattern
- Kafka tests require `@EmbeddedKafka`

**Kotlin style:** Extension properties on domain objects are used heavily inside service classes (e.g., `val Melding.prioritetFom`, `val Vedtak.underArt`) — prefer this pattern over utility functions for domain enrichment.

**Profiles:** `dev` and `prod` — upstream URLs and Kafka topics differ per profile (see `application-dev.yaml` / `application-prod.yaml`).
