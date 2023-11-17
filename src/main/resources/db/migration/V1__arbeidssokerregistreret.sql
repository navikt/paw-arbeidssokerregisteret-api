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

CREATE TYPE Beskrivelse AS ENUM (
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

CREATE TABLE arbeidssokerperioder
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
    utdanningsnivaa Utdanningsnivaa NOT NULL,
    bestatt JaNeiVetIkke NOT NULL,
    godkjent JaNeiVetIkke NOT NULL
);

CREATE TABLE helse
(
    id BIGSERIAL PRIMARY KEY,
    helsetilstandHindrerArbeid JaNeiVetIkke NOT NULL
);

CREATE TABLE arbeidserfaring
(
    id BIGSERIAL PRIMARY KEY,
    harHattArbeid JaNeiVetIkke NOT NULL
);

CREATE TABLE situasjon
(
    id BIGSERIAL PRIMARY KEY,
    periode_id UUID NOT NULL,
    sendt_inn_av_id BIGINT REFERENCES metadata(id),
    utdanning_id BIGINT REFERENCES utdanning(id),
    helse_id BIGINT REFERENCES helse(id),
    arbeidserfaring_id BIGINT REFERENCES arbeidserfaring(id)
);

CREATE TABLE arbeidssokersituasjon
(
    id BIGSERIAL PRIMARY KEY,
    situasjon_id BIGINT REFERENCES situasjon(id)
);

CREATE TABLE beskrivelsemeddetaljer
(
    id BIGSERIAL PRIMARY KEY,
    arbeidssokersituasjon_id BIGINT REFERENCES arbeidssokersituasjon(id),
    beskrivelse Beskrivelse NOT NULL
);

CREATE TABLE detaljer
(
    id BIGSERIAL PRIMARY KEY,
    beskrivelse_med_detaljer_id BIGINT REFERENCES beskrivelsemeddetaljer(id),
    nokkel VARCHAR(50),
    verdi VARCHAR(255)
);



