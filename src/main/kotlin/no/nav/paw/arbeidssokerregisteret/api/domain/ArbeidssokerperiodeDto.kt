package no.nav.paw.arbeidssokerregisteret.api.domain

import java.time.LocalDateTime
import java.util.*

data class ArbeidssokerperiodeDto(
    val id: UUID,
    val identitetsnummer: String,
    val startet: Metadata,
    val avsluttet: Metadata? = null
)

data class Metadata(
    val tidspunkt: LocalDateTime,
    val utfoertAv: Bruker,
    val kilde: String,
    val aarsak: String
)

data class Bruker(
    val type: BrukerType,
    val id: String
)

enum class BrukerType { UKJENT_VERDI, UDEFINERT, VEILEDER, SYSTEM, SLUTTBRUKER }
