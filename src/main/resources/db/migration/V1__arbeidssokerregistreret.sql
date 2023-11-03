CREATE TYPE BrukerType AS ENUM (
    'UKJENT_VERDI', 'UDEFINERT', 'VEILEDER', 'SYSTEM', 'SLUTTBRUKER'
    );

CREATE TABLE bruker
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    bruker_id VARCHAR(255) NOT NULL,
    type BrukerType NOT NULL,
    unique (bruker_id, type)
);

CREATE TABLE metadata
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    utfoert_av_id bigint REFERENCES bruker(id),
    tidspunkt TIMESTAMP(6) NOT NULL,
    kilde VARCHAR(255) NOT NULL,
    aarsak VARCHAR(255) NOT NULL
);

CREATE TABLE arbeidssokerperioder
(
    id bigint UNIQUE GENERATED ALWAYS AS IDENTITY,
    periode_id UUID NOT NULL,
    identitetsnummer VARCHAR(11)  NOT NULL,
    startet_id bigint REFERENCES  metadata(id),
    avsluttet_id bigint REFERENCES  metadata(id),
    unique (periode_id)
);