# Ytterpunkter: dataflyt inn og ut av paw-namespacet

Skissen viser hvor data kommer inn til arbeidssøkerregisteret utenfra, og hvor data forlater
paw-namespacet. Utgående data er delt i to: data som blir i Nav, og data som deles eksternt via
Maskinporten.

Kilder: [paw-iac](https://github.com/navikt/paw-iac) (Kafka-ACL-er), README og
[docs/meldingsautentisitet-og-integritet.md](meldingsautentisitet-og-integritet.md) i dette
repoet, samt kildekode i
[paw-arbeidssoekerregisteret-monorepo-intern](https://github.com/navikt/paw-arbeidssoekerregisteret-monorepo-intern)
og [paw-arbeidssoekerregisteret-monorepo-ekstern](https://github.com/navikt/paw-arbeidssoekerregisteret-monorepo-ekstern).

For Kafka-topics er reglen: ekstern READ-tilgang i ACL regnes som data ut, ekstern WRITE-tilgang
regnes som data inn. For REST-API-er er reglen tilsvarende: en `accessPolicy.inbound`-regel fra en
applikasjon utenfor paw-namespacet (annet `namespace` enn `paw`, eller uten namespace når kallende
app selv ligger i et annet namespace) regnes som data ut. Alle nais.yaml-filer under er hentet fra
`monorepo-intern` (commit `c41b654`) og `monorepo-ekstern` (commit `c00a502`).

I skissen er alt som ligger **innenfor den blå boksen** («paw-namespace») det som faktisk er
arbeidssøkerregisteret: applikasjonene og Kafka-topicene som paw-teamet selv eier og drifter.
Alt utenfor boksen, uansett om det er til venstre (sender inn) eller til høyre (mottar ut), er
andre team, systemer eller sluttbrukere, ikke en del av arbeidssøkerregisteret.

```mermaid
flowchart LR
    subgraph inn["Utenfor paw – sender inn"]
        Bruker["Sluttbruker (innbygger)"]
        Veileder["Veileder (Nav-ansatt)"]
        PaaVegneAvInn["team dagpenger + team flex\n(sender bekreftelse på vegne av bruker)"]
    end

    subgraph paw["paw-namespace (= arbeidssøkerregisteret)"]
        Inngang["api-start-stopp-perioder"]
        BekreftelseApi["bekreftelse-api"]
        Hendelsefilter["bekreftelse-hendelsefilter"]
        ArenaAdapter["arena-adapter"]
        EksterntApi["eksternt-api"]
        OppslagApi["oppslag-api (v2)"]
        Topics[("Kafka-topics:\nperioder, opplysninger,\nprofilering, bekreftelse,\narena")]
    end

    subgraph nav_ut["Blir i Nav"]
        KafkaKonsumenter["Kafka READ:\nteam dagpenger, team obo,\ndv-a, teamarenanais"]
        RestKonsumenter["REST mot oppslag-api:\nteam obo, teamdagpenger, dab,\nflex, teamcrm/platforce,\nteamsykefravr, teamfamilie,\npoao, personoversikt,\nteamarenanais"]
    end

    subgraph maskinporten_ut["Deles eksternt via Maskinporten"]
        Lanekassen["Lånekassen\n(eneste konsument per i dag)"]
    end

    Bruker -- "IdPorten" --> Inngang
    Bruker -- "IdPorten" --> BekreftelseApi
    Veileder -- "Azure AD, på vegne av bruker" --> Inngang
    PaaVegneAvInn -- "Kafka WRITE" --> Hendelsefilter

    Inngang --> Topics
    BekreftelseApi --> Topics
    Hendelsefilter --> Topics
    Topics --> ArenaAdapter --> Topics
    Topics --> OppslagApi
    Topics --> EksterntApi

    Topics -- "Kafka READ-ACL" --> KafkaKonsumenter
    OppslagApi -- "accessPolicy.inbound" --> RestKonsumenter
    EksterntApi -- "REST + Maskinporten" --> Lanekassen
```

Nøyaktig hvilke applikasjoner som har tilgang, står i tabellene under.

## Inngangspunkter

| Kilde | Møtepunkt i paw | Mekanisme | Kilde/referanse |
|---|---|---|---|
| Sluttbruker (innbygger) | `api-start-stopp-perioder` | IdPorten (HTTP) | `apps/api-start-stopp-perioder`, monorepo-intern |
| Veileder (Nav-ansatt) | `api-start-stopp-perioder` | Azure AD, på vegne av bruker | `apps/tilgangskontroll/AuthProviders.kt`, monorepo-intern |
| Sluttbruker (innbygger) | `bekreftelse-api` | IdPorten | `apps/bekreftelse-api`, monorepo-intern |
| team dagpenger (`dp-rapportering-personregister`, `dp-meldekort`) | `paw.arbeidssoker-bekreftelse-paavegneav-teamdagpenger-v2` | Kafka, ekstern WRITE (readwrite-ACL) | `kafka/prod/bekreftelse/eksterne/*teamdagpenger-v2.yaml`, paw-iac |
| team flex (`flex-arbeidssokerregister-oppdatering`) | `paw.arbeidssoker-bekreftelse-paavegneav-friskmeldt-til-arbeidsformidling-v1` | Kafka, ekstern WRITE (readwrite-ACL) | `kafka/prod/bekreftelse/eksterne/*friskmeldt*.yaml`, paw-iac |

`bekreftelse-hendelsefilter` leser disse eksterne «på vegne av»-topicene og republiserer med
PAW-signatur til `paw.arbeidssoker-bekreftelse-v1` (se
[docs/meldingsautentisitet-og-integritet.md](meldingsautentisitet-og-integritet.md)).

`arena-adapter` leser fra `paw.arbeidssokerperioder-v1`, `paw.arbeidssoker-profilering-v1`,
`paw.opplysninger-om-arbeidssoeker-v1` og `paw.arbeidssoker-bekreftelse-v1` (alle med Kafka READ,
team paw), slår sammen (join) disse og skriver et beriket resultat til
`paw.arbeidssoker-arena-v1`. team teamarenanais (`arena-hendelse`) har Kafka READ på dette topicet
og henter dataene derfra.

## Utgangspunkter som blir i Nav

**Kafka:**

| Topic | Ekstern konsument | Tilgang | Kilde/referanse |
|---|---|---|---|
| `paw.arbeidssokerperioder-v1` | team dagpenger: `dp-meldekort`, `dp-oppslag-arbeidssoker`, `dp-rapportering-personregister` | Kafka READ | `kafka/prod/paw.arbeidssokerperioder-v1.yaml`, paw-iac |
| `paw.arbeidssoker-profilering-v1` | team obo: `veilarbportefolje` | Kafka READ | `kafka/prod/paw.arbeidssoker-profilering-v1.yaml`, paw-iac |
| `paw.opplysninger-om-arbeidssoeker-v1` | team obo: `veilarbportefolje` | Kafka READ | `kafka/prod/paw.opplysninger-om-arbeidssoeker-v1.yaml`, paw-iac |
| `paw.arbeidssoker-bekreftelse-v1` | `dv-a-team-arbeidssoker-konsument` | Kafka READ | `kafka/prod/bekreftelse/paw.arbeidssoker-bekreftelse-v1.yaml`, paw-iac |
| `paw.arbeidssoker-arena-v1` | team teamarenanais: `arena-hendelse` | Kafka READ | `kafka/prod/paw.arbeidssoker-arena-v1.yaml`, paw-iac |

**REST (`oppslag-api-v2`)**: dette er hovedkanalen for intern datadeling i Nav, med mange
konsumenter. Alle er registrert som `accessPolicy.inbound`-regler i
`apps/oppslag-api-v2/nais/nais-prod.yaml` (monorepo-ekstern):

| Team (namespace) | Applikasjoner |
|---|---|
| `teamarenanais` | `arena` |
| `dab` | `ao-oppfolgingskontor`, `veilarbdirigent` |
| `flex` | `flex-arbeidssokerregister-oppdatering`, `sykepengesoknad-backend` |
| `obo` | `veilarbperson`, `veilarbportefolje`, `veilarbvedtaksstotte` |
| `teamdagpenger` | `dp-oppslag-arbeidssoker`, `dp-rapportering-personregister`, `dp-meldekortregister`, `dp-meldekort`, `dp-rapportering`, `dp-soknadsdialog`, `dp-mine-dagpenger-frontend`, `dp-brukerdialog-frontend` |
| `teamcrm` / `platforce` | `saas-proxy` (trolig Salesforce-integrasjon, f.eks. Nav Kontaktsenter) |
| `teamsykefravr` | `isfrisktilarbeid` |
| `teamfamilie` | `familie-ef-sak` |
| `poao` | `veilarboppfolging` |
| `personoversikt` | `modiapersonoversikt-api` |

**REST (`egenvurdering-dialog-tjeneste`)**: `obo`-teamets `veilarbvedtaksstotte` har
`accessPolicy.inbound`-tilgang (`apps/egenvurdering-dialog-tjeneste/nais/nais-prod.yaml`,
monorepo-ekstern). Dette henger sammen med `paw.beriket-14a-vedtak-v1`, som i dag ikke har noen
ekstern Kafka-READ-ACL. 14a-vedtaksdata når altså `obo`/`veilarbvedtaksstotte` via REST, ikke
Kafka.

Interne paw-apper som `paw-brukerstotte` og de ulike frontendene
(`arbeidssokerregistrering(-for-veileder)`, `arbeidssoekerregisteret-for-personbruker`,
`aia-backend`, `aia-min-side-ssr`) kaller også inn til `oppslag-api-v2`, `bekreftelse-api`,
`egenvurdering-api` og `mine-stillinger-api`, men disse ligger selv i `namespace: paw` og regnes
derfor ikke som data ut av namespacet.

## Utgangspunkt som deles eksternt via Maskinporten

| API | Endepunkt | Scope | Kilde/referanse |
|---|---|---|---|
| `eksternt-api` | `POST /api/v1/arbeidssoekerperioder` | `nav:arbeid:arbeidssokerregisteret.read` | `apps/eksternt-api`, monorepo-ekstern (`nais-prod.yaml`, `security_config.toml`, `PeriodeRoutes.kt`) |

**Kjent konsument:** Lånekassen er i dag den eneste eksterne virksomheten med tilgang via
Maskinporten, ikke en av flere. Maskinporten-tilganger registreres i Maskinportens eget
administrasjonsgrensesnitt, ikke i Nav-repoene, så opplysningen er ikke verifisert i kildekoden.
Den er lagt inn på bakgrunn av kjent informasjon.

## Åpne punkter / ikke bekreftet

* `saas-proxy` (`teamcrm`/`platforce`) sin bruk av oppslagsdataene er antatt, ikke bekreftet mot
  dokumentasjon utenfor nais.yaml.
* Frontend-repoer (f.eks. bruker-vendt UI) er ikke gjennomgått i detalj. Inngangspunktet er
  identifisert via IdPorten-oppsettet i `api-start-stopp-perioder` og `bekreftelse-api`, og via
  `accessPolicy.inbound` i konsumerende API-er, ikke via selve frontend-koden.
