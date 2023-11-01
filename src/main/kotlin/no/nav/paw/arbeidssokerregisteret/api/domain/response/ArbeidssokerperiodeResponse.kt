package no.nav.paw.arbeidssokerregisteret.api.domain.response

import java.time.LocalDateTime

data class ArbeidssokerperiodeResponse(
    val startet: LocalDateTime,
    val avsluttet: LocalDateTime? = null
)
