# paw-arbeidssokerregisteret-api
1. [Hvordan fungerer det](#hvordan-fungerer-det)
2. [Kafka topics](#kafka-topics)
   1. [Periode topic](#periode-topic)
   2. [Opplysninger om arbeidssøker topic](#opplysninger-om-arbeidssoker-topic)
   3. [Profilerings topic](#profilerngs-topic)
   4. [Arena topic](#arena-topic)
3. REST API
   1. [Oppslag API (internt for NAV)](https://github.com/navikt/paw-arbeidssoekerregisteret-api-oppslag)
   2. [Eksternt API](https://github.com/navikt/paw-arbeidssoekerregisteret-eksternt-api)
   3. [Start/Stopp av Perioder](https://github.com/navikt/paw-arbeidssokerregisteret-api-inngang)
  
## Hvordan Fungerer Det
Arbeidssøkerregisteret er basert på arbeidssøkerperioder. En periode har alltid en startet dato og får en avsluttet dato så snart den avsluttes. En person kan ha 0 eller 1 aktive perioder til en hver tid.

I tillegg til perioden inneholder også registeret opplysninger om arbeidssøker samt. resultatet av profilering av arbeidssøker. 
Profileringen gjøres på bakgrunn av opplysningene og brukes som beslutningsstøtte i veileders vurderingen av brukerens oppfølgingsbehov. 
Innsendig av opplysninger er valgfritt og det er derfor ikke gitt at vi har opplysninger om en person. 
Har vi ikke opplysninger har vi heller ikke noe profileringsresultat.

Informasjonen i registeret kan hentes enten ved å abonnere på de aktuelle Kafka topicene eller å bruke oppslag API.

Enkel oversikt over hva som skjer når når en periode startes via API:
```mermaid
sequenceDiagram
    HttpClient->>InngangApi: (HTTP POST) Send start forespørsel
    InngangApi->>PoaoTilgang: Dersom veileder, sjekk tilgang til person    
    InngangApi->>PDL: Hent person info
    InngangApi->>KafkaKey: Hent kafka key for person
    InngangApi->>Kafka: Publiser startet eller avvist hendelse til loggen
    InngangApi->>HttpClient: Svar 200 eller feilkode.
    Kafka->>HendelseHåndterer: Motta hendelse fra loggen
    HendelseHåndterer->>Kafka: Filtrer hendelse opp mot gjeldene tilstand i Kafka state store
    HendelseHåndterer->>Kafka: Publiser periode record        
```

## Kafka Topics
Viktige punkter angående Kafka topics:
* Tilgang til topics er styrt av ACL. Den foretrukne måten å be om tilgang på er via PR til paw-iac. Her finner man config for de aktuelle topicene. Skrivetilgang til repoet er begrenset så workflowen blir 'fork' + 'PR'. Husk å inkludere relevant info for tilgang (link til behandlingskatalog) i PR.
* Alle topics er co-partitioned, dvs. likt antall partisjoner og for en gitt person vil alle records havne på samme pertisjon. Dette betyr blant annet at man kan lage enkle egne Kafka Streams joins operasjoner (join Periode, Opplysninger, Profilerig) på PeriodeId uten å måtte repartisjonere. Alt som skal til er en enkel KeyValue store med [TopicJoin](`helpers/topics_join-v4.avdl`).
* Record.key for topics knyttet til arbeidssøkerregisteret er ikke unike og brukes utelukkende til å garantere at alle meldinger for en gitt person publiseres på samme partisjon for et topic med 6 partisjoner. Dette betyr at Record.key ikke kan brukes til å se om to meldinger tilhører samme person. Det er viktig å være klar over at dette gjelder begge veier. Det at to meldinger har forskjellige Record.key betyr ikke at de ikke er for samme person. 

Registeret består av 3 kafka topics. Meldingsformatet er Avro og skjema er tilgjengelig i dette repoet, blant annet som [maven artifacter](https://github.com/navikt/paw-arbeidssokerregisteret-api/releases).
For kotlin/java prosjekter kan man enkelt generere nødvendige klasser via et gradle plugin. Eksempel fra build.gradle.kts i [Hendelse håndtering](https://github.com/navikt/paw-arbeidssokerregisteret-event-prosessor):
```kotlin
import com.github.davidmc24.gradle.plugin.avro.GenerateAvroProtocolTask

plugins {
    kotlin("jvm")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"    
}

val arbeidssokerregisteretSchemaVersion = "1.7843506781.4-1"

val schema by configurations.creating {
  isTransitive = false
}

dependencies {
    schema("no.nav.paw.arbeidssokerregisteret.api:main-avro-schema:$arbeidssokerregisteretSchemaVersion")
    implementation("org.apache.avro:avro:1.11.0")
}

tasks.named("generateAvroProtocol", GenerateAvroProtocolTask::class.java) {
  schema.forEach {
    source(zipTree(it))
  }
}
```

 

### Versjonering
Avro schema og topics er versjonert. Ved endringer som ikke er bakoverkompatible vil følgende gjøres:
* Den aktuelle delen i schema endres fra -v(x) til -v(x+1), feks periode-v1.avdl til periode-v2.avdl (samme edring gjøres også for namespace i avro filen)
* Major version på schema artifactet økes med 1, feks fra 1.123.2-1 til 2.123.5-1.
* Berørte topics for en ny versjon, f.eks: paw.arbeidssokerperioder-v1 blir til paw.arbeidssokerperioder-v2
* Gamle topics går over i vedlikeholdsmodus, dvs. nye data blir publisert, men det blir ingen oppdateringer av selve schemaet og ingen nye applikasjoner legges til i ACL. 
* Nye topics spoles opp og vil inneholde alt av data, record key, Periode.id og OpplysningerOmArbeidssoeker.id vil forbli uendret i ny topic. Så dersom en periode-v1 har samme id som en periode-v2 er det den samme perioden. 

Hvor lenge topics blir gående i vedlikeholds modus er ikke helt avgjort. I enkelte tilfeller vil eksterne endringer gjøre at det i praksis ikke blir mulig å vedlikeholde eldre topics. F.eks: dersom data som var obligatorisk ikke lenger er tilgjengelig eller ikke lenger er lov å samle inn.

Konsumenter som skal bytte til en ny topic versjon må håndtere dette på en måte. Her er det flere muligheter, f.eks: bruke Periode.id og OpplysningerOmArbeidssoeker.id for å holde orden på hva som allerede er håndtert. Det er også mulig å lage høyvannsmerker basert på record.key, record.timestamp og topic.
Timestamp settes når vi godtar en ekstern forespørsel, så en slik løsning vil måtte leve med den teoretiske muligheten for at en ny melding kan være eldre enn forrige melding fra samme topic med samme key. Å bruke offset er trolig den minst trygge måten å gjøre det på siden den nye versjonen kanskje kan inneholde færre eller flere meldinger enn den forrige versjonen av topicet. F.eks: ved endringer i hva som er gyldig/ikke gyldig data.

### Periode Topic
Topic navn: `paw.arbeidssokerperioder-{VERSION}`  
Gjeldene versjon: `v1`  
Schema: [periode](main-avro-schema/src/main/resources/periode-v1.avdl)

* Innholder samtlige arbeidssøkerperioder.
* Alle perioder har en start dato.
* Når de avsluttes settes tidspunkt for avslutning. Dette tidspunktet vil aldri være frem i tid.
* Identitetsnummeret kan endre seg, det vil da komme en ny record hvor identitetsnummeret er endret.
* Record timestamp matcher tidspunktet vi godtok 'start' forespørselen.

### Opplysninger Om Arbeidssoker Topic
Topic navn: `paw.opplysninger-om-arbeidssoeker-{VERSION}`  
Gjeldene versjon: `v1`
Schema: [opplysninger_om_arbeidssoker](main-avro-schema/src/main/resources/opplysninger_om_arbeidssoeker-v4.avdl)

Inneholder opplysninger om arbeidssøker. Opplysningene er knyttet til en periode og en periode kan ha flere records med opplysninger knyttet til seg. I enkelte tilfeller vil systemet gjenbruke opplysninger når følgende hendelser inntreffer iløpet av 60 sekunder:
1. Opplysninger sendes in for den aktive perioden (referert til som A)
2. Perioden A avsluttes.
3. En ny periode B startes.

I slike tilfeller vil man først se at opplysningene som publiseres er knyttet til periode A, også kort tid etterpå publiseres opplysningene på nytt med samme opplysning id, men denne gangen knyttet til periode B.  
Endringer av opplysninger vil alltid føre til en ny record med ny opplysning id.   
Record timestamp matcher tidspunktet vi mottok opplysningene.


### Profilerings Topic
Topic navn: `paw.arbeidssoker-profilering-{VERSION}`  
Gjeldene versjon: `v1`  
Schema: [periode](main-avro-schema/src/main/resources/profilering-v1.avdl)

Inneholder resultatet av profileringen som gjøres når det sendes inn opplysninger.
Kun opplysninger sendt inn etter 1.1 2024 er profilert i nytt register.
En profilering vil alltid være tilknyttet en opplysning id og dermed også en periode id.
Record timestamp matcher i praksis record timestamp for opplysningene, men kan i noen tilfeller (f.eks: for migrert data) matche record timestamp for perioden.  
Profilering.sendtInnAv.tidspunkt kan benyttes for å se når selve profilering ble utført. Så i praksis viser Record timestamp når profileringen ideelt sett gjelder fra, og '....tidspunkt' viser når den ble utført. NB: Setting av Record timestamp ble endret etter at initiell import av gammel data var ferdig slik at det nå er tidspunkt for når profileringen ble utført som brukes.

### Bekreftelse Topic
Topic navn: `paw.arbeidssoker-bekreftelse-{VERSION}`
Gjeldene versjon: `beta-v1`
Schema: [bekreftelse](bekreftelsesmelding-schema/src/main/resources/bekreftelsesmelding-v1.avdl)
Hver X. dag (normalt hver 14. dag, men konsumenter må takle at dette intervallet endres) må bruker bekrefte av vedkommende fremdeles ønsker å være arbeidssker og oppgit om vedkommende har jobbet i den aktuelle perioden. Dersom vedkommende ikke lenger ønsker å være arbeidssøker blir perioden avsluttet.
Dette topicet vil inneholde svar fra alle arbeidssøkere uavhengig av om de har ytelser eller annet som har egne bekreftelsesrutiner.

### Arena Topic
Topic navn: `paw.arbeidssoker-arena-{VERSION}`
Gjeldene versjon: `v1`
Schema: [arena](arena-avro-schema/src/main/resources/arena-v5.avdl)

Topic utelukkende for Arena. Endringer kan forekomme uten forvarsel basert på interne diskusjoner med Arena utviklere og topic blir slettet så snart Arena ikke lenger har behov for det.  
`arena-avro-schema/src/main/resources/` inneholder en enkel fil med en record. `avdl`-filene som importeres opprettes som en del av byggeprosessen og de er basert på innholdet i main-avro-schema.
