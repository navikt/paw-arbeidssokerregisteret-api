CREATE TYPE BrukerType AS ENUM (
    'UKJENT_VERDI', 'UDEFINERT', 'VEILEDER', 'SYSTEM', 'SLUTTBRUKER'
    );

CREATE TABLE bruker
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    "brukerId" VARCHAR(255) NOT NULL,
    type BrukerType NOT NULL
);

CREATE TABLE metadata
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    "utfoertAvId" bigint REFERENCES bruker(id),
    tidspunkt TIMESTAMP(6) NOT NULL,
    kilde VARCHAR(255) NOT NULL,
    aarsak VARCHAR(255) NOT NULL
);

CREATE TABLE arbeidssokerperioder
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    "periodeId" UUID NOT NULL,
    identitetsnummer VARCHAR(11)  NOT NULL,
    "startetId" bigint REFERENCES  metadata(id),
    "avsluttetId" bigint REFERENCES  metadata(id)
);