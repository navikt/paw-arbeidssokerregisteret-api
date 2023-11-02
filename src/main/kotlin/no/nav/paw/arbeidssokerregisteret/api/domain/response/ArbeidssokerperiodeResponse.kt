package no.nav.paw.arbeidssokerregisteret.api.domain.response

import java.time.Instant

data class ArbeidssokerperiodeResponse(
    val startet: Instant,
    val avsluttet: Instant? = null
)
