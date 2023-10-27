CREATE TABLE arbeidssokerperioder
(
    id             SERIAL PRIMARY KEY,
    foedselsnummer VARCHAR(11)  NOT NULL,
    startet        TIMESTAMP(6) NOT NULL,
    avsluttet      TIMESTAMP(6)
);