# Flyt: `POST /api/refusjonskrav`

Forenklet flytdiagram for branchen `feature/refusjonskrav_service`, fra `RefusjonskravController.opprett()`.

```mermaid
flowchart TD
    A([POST /api/refusjonskrav]) --> B["Controller<br/>autentisering og validering av request"]
    B --> C["Hent melding fra SAM"]
    C --> D{"Kall via Maskinporten?"}
    D -- ja --> E["Sjekk at tpnr tilhører organisasjonen (TP)"]
    D -- nei --> F
    E --> F["Valider krav mot melding<br/>pid, tpnr, ikke besvart, trekkperioder"]
    F --> G{"Har trekk?"}

    G -- nei --> H["Registrer svar i SAM:<br/>ingen refusjonskrav"]
    H --> Z1([204 NO_CONTENT])

    G -- ja --> I["Opprett hendelse i SAM"]
    I --> J["Registrer svar i SAM:<br/>refusjonskrav"]
    J --> K["Opprett andre trekk i OS<br/>(ytelser og tss-id fra TP, kreditor fra konfig)"]
    K --> L{"Alle meldinger<br/>på vedtaket besvart?"}
    L -- nei --> Z1
    L -- ja --> M{"Fagområde"}
    M -- PEN --> N["Varsle PEN"]
    M -- "EYO / AAP" --> O["Send Kafka-melding"]
    N --> P["Sett vedtak BESVART i SAM"]
    O --> P
    P --> Z1

    N -. feil .-> R["Sett vedtak IKKE_OVERFORT_PEN"]
    O -. feil .-> R
    P -. feil .-> R
    R --> Z2([202 ACCEPTED<br/>krav registrert, lukking avventer])

    B -. feil .-> X1([401 / 403 / 400 / 503])
    C -. feil .-> X2([404 / 502])
    E -. feil .-> X3([403 / 502])
    F -. feil .-> X4([400 / 409])
    I -. feil .-> X2
    J -. feil .-> X2
    K -. feil .-> X2

    classDef ok fill:#E8F5E9,stroke:#007C2E,color:#005A1F;
    classDef warn fill:#FFF8E1,stroke:#C17F00,color:#7A5200;
    classDef err fill:#FFEBEE,stroke:#C30000,color:#8F0000;
    class Z1 ok;
    class Z2 warn;
    class X1,X2,X3,X4 err;
```

## Merknader

- Før controlleren kjører pinger et filter OS, PEN, SAM og TP. Er en av dem nede, gir kallet 503.
- Bare feil ved lukking av vedtak gir 202. Kravet er da lagret, men vedtaket er ikke lukket.
- Stiplede piler viser feil. Feilkodene er gruppert: 400 og 409 er valideringsfeil, 404 og 502 er feil mot SAM, TP eller OS.
