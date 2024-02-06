# paw-arbeidssokerregisteret-api

Arbeidssøkerregisteret er basert på arbeidssøkerperioder. En periode har alltid et start dato og får en avsluttnings dato så snart den avsluttes. En person kan 0 eller 1 aktive perioder til en hver tid. 

I tillegg til perioden inneholder også registeret en del opplysninger om arbeidssøkeren samt resultatet av profilering av arbeidssøkeren. Profileringen gjøre på bakgrunn av opplysningene og brukes til å gi brukeren behovs tilpasset oppføling. Innesendig av opplysninger er valgfritt og det er derfor ikke gitt at vi har opplysninger om en gitt person. Har vi ikke opplsyninger har vi heller ikke noe profileringsresultat. 

* Kafka topics
  * [Periode topic](doc/periode_topic.md)
  * [Opplysninger om arbeidssøker topic](doc/opplysninger_topic.md)
  * [Profilerings topic](doc/profilering_topic.md)
* REST API
  * [Søke API](https://github.com/navikt/paw-arbeidssokerregisteret-api-soek)
  * [Start/Stopp av Perioder](https://github.com/navikt/paw-arbeidssokerregisteret-api-inngang)
  
