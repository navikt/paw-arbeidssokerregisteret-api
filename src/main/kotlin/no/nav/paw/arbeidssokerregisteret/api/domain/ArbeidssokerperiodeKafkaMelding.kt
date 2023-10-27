package no.nav.paw.arbeidssokerregisteret.api.domain

import java.time.LocalDateTime

data class ArbeidssokerperiodeKafkaMelding(
    val startet: LocalDateTime,
    val avsluttet: LocalDateTime?
)
