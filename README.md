# paw-arbeidssokerregisteret-api

Arbeidssøkerregisteret er basert på arbeidssøkerperioder. En periode har alltid en start dato og får en avsluttnings dato så snart den avsluttes. En person kan 0 eller 1 aktive perioder til en hver tid. 

I tillegg til perioden inneholder også registeret en del opplysninger om arbeidssøkeren samt resultatet av profilering av arbeidssøkeren. Profileringen gjøre på bakgrunn av opplysningene og brukes til å gi brukeren behovs tilpasset oppføling. Innesendig av opplysninger er valgfritt og det er derfor ikke gitt at vi har opplysninger om en gitt person. Har vi ikke opplsyninger har vi heller ikke noe profileringsresultat. 


* Kafka topics
  * [Periode topic](doc/periode_topic.md)
  * [Opplysninger om arbeidssøker topic](doc/opplysninger_topic.md)
  * [Profilerings topic](doc/profilering_topic.md)
* REST API
  * [Søke API (internt for NAV)](https://github.com/navikt/paw-arbeidssokerregisteret-api-soek)
  * [Eksternt API](https://github.com/navikt/paw-arbeidssokerregisteret-eksternt-api)
  * [Start/Stopp av Perioder](https://github.com/navikt/paw-arbeidssokerregisteret-api-inngang)
  
* Repoer
  * [Start/Stopp av Perioder](https://github.com/navikt/paw-arbeidssokerregisteret-api-inngang)
  * [Hendelse håndtering](https://github.com/navikt/paw-arbeidssokerregisteret-event-prosessor)
  * [Søke API(internt for NAV)](https://github.com/navikt/paw-arbeidssokerregisteret-api-soek)
  * [Eksternt API](https://github.com/navikt/paw-arbeidssokerregisteret-eksternt-api)
  * [Migrering]()
  * [Arena adapter](https://github.com/navikt/paw-arbeidssokerregisteret-arena-adapter)
  * [Feilsøkings verktøy](https://github.com/navikt/paw-arbeidssokerregisteret-feilsoking)
  
# Kort om de forskjellige delene:

## Start/stopp av perioder
Enkelt API for å starte og stoppe perioder. Dette vil være den eneste inngangen til registeret.

## Hendelse håndtering
Dette er logikken i selve registeret. Den tar imot hendelser fra Start/Stopp API og andre fremtidige tjenester og sjekker disse opp mot gjeldene tilstand for den aktuelle brukeren før den eventuelt publiserer en Periode-Startet/Avsluttet eller opplysninger-mottatt melding på Kafka topics.

## Søke API
API for å søke i arbeidssøkerperioder, opplysninger og profileringer. Får data via topics for perioder, opplysninger og profileringer.  
Er bare tilgjengelig internt i NAV.

## Eksternt API
En forenklet utgave av Søke API, men enkel info om perioder. Denne henter også data direkte fra Kafka topics. Denne er tilgjengelig utenfor NAV via maskinporten autentisering.

## Migrering
Tjeneste som kontinuerlig importerer data fra veilarbregistrering og besvarelse til det nye registeret. Denne vil bli deaktivert når nytt register blir master.

## Arena Adapter
Adapter som samler data fra periode, opplysninger og profilering topics til et topic. Kun tilgjengelig for Arena og vil bli slettet så snart Arena ikke trenger det lenger.

## Feilsøking
Div verktøy for feilsøking i nytt register.