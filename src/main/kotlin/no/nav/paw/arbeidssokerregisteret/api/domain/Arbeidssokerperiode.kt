package no.nav.paw.arbeidssokerregisteret.api.domain

import java.time.LocalDateTime

data class Arbeidssokerperiode(
    val startet: LocalDateTime,
    val avsluttet: LocalDateTime? = null
)
