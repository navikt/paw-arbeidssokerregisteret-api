CREATE TYPE BrukerType AS ENUM (
    'UKJENT_VERDI', 'UDEFINERT', 'VEILEDER', 'SYSTEM', 'SLUTTBRUKER'
    );

CREATE TYPE Utdanningsnivaa AS ENUM (
    'UKJENT_VERDI', 'UDEFINERT', 'INGEN_UTDANNING', 'GRUNNSKOLE', 'VIDEREGAENDE_GRUNNUTDANNING', 'VIDEREGAENDE_FAGBREV_SVENNEBREV', 'HOYERE_UTDANNING_1_TIL_4',
    'HOYERE_UTDANNING_5_ELLER_MER'
    );

CREATE TYPE JaNeiVetIkke AS ENUM (
    'JA', 'NEI', 'VET_IKKE'
    );

CREATE TYPE BeskrivelseEnum AS ENUM (
    'UKJENT_VERDI',
    'UDEFINERT',
    'HAR_SAGT_OPP',
    'HAR_BLITT_SAGT_OPP',
    'ER_PERMITTERT',
    'ALDRI_HATT_JOBB',
    'IKKE_VAERT_I_JOBB_SISTE_2_AAR',
    'AKKURAT_FULLFORT_UTDANNING',
    'VIL_BYTTE_JOBB',
    'USIKKER_JOBBSITUASJON',
    'MIDLERTIDIG_JOBB',
    'DELTIDSJOBB_VIL_MER',
    'NY_JOBB',
    'KONKURS',
    'ANNET'
    );

CREATE TABLE bruker
(
    id BIGSERIAL PRIMARY KEY,
    bruker_id VARCHAR(255) NOT NULL,
    type BrukerType NOT NULL,
    UNIQUE (bruker_id, type)
);

CREATE TABLE metadata
(
    id BIGSERIAL PRIMARY KEY,
    utfoert_av_id BIGINT REFERENCES bruker(id),
    tidspunkt TIMESTAMP(6) NOT NULL,
    kilde VARCHAR(255) NOT NULL,
    aarsak VARCHAR(255) NOT NULL
);

CREATE TABLE periode
(
    id BIGSERIAL PRIMARY KEY,
    periode_id UUID NOT NULL,
    identitetsnummer VARCHAR(11) NOT NULL,
    startet_id BIGINT REFERENCES  metadata(id),
    avsluttet_id BIGINT REFERENCES  metadata(id),
    UNIQUE (periode_id)
);

CREATE TABLE utdanning
(
    id BIGSERIAL PRIMARY KEY,
    lengde Utdanningsnivaa NOT NULL,
    bestaatt JaNeiVetIkke NOT NULL,
    godkjent JaNeiVetIkke NOT NULL
);

CREATE TABLE helse
(
    id BIGSERIAL PRIMARY KEY,
    helsetilstand_hindrer_arbeid JaNeiVetIkke NOT NULL
);

CREATE TABLE arbeidserfaring
(
    id BIGSERIAL PRIMARY KEY,
    har_hatt_arbeid JaNeiVetIkke NOT NULL
);

CREATE TABLE annet
(
    id BIGSERIAL PRIMARY KEY,
    andre_forhold_hindrer_arbeid JaNeiVetIkke NOT NULL
);

CREATE TABLE opplysninger_om_arbeidssoeker
(
    id BIGSERIAL PRIMARY KEY,
    opplysninger_om_arbeidssoeker_id UUID NOT NULL,
    periode_id UUID REFERENCES periode(periode_id),
    sendt_inn_av_id BIGINT REFERENCES metadata(id),
    utdanning_id BIGINT REFERENCES utdanning(id),
    helse_id BIGINT REFERENCES helse(id),
    arbeidserfaring_id BIGINT REFERENCES arbeidserfaring(id),
    annet_id BIGINT REFERENCES annet(id)
);

CREATE TABLE beskrivelse_med_detaljer
(
    id BIGSERIAL PRIMARY KEY,
    opplysninger_om_arbeidssoeker_id BIGINT REFERENCES opplysninger_om_arbeidssoeker(id)
);

CREATE TABLE beskrivelse
(
    id BIGSERIAL PRIMARY KEY,
    beskrivelse BeskrivelseEnum NOT NULL,
    beskrivelse_med_detaljer_id BIGINT REFERENCES beskrivelse_med_detaljer(id)
);

CREATE TABLE detaljer
(
    id BIGSERIAL PRIMARY KEY,
    beskrivelse_id BIGINT REFERENCES beskrivelse(id),
    noekkel VARCHAR(50),
    verdi VARCHAR(255)
);
