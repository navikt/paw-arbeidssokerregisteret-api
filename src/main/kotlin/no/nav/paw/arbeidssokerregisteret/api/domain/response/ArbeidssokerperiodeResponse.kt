package no.nav.paw.arbeidssokerregisteret.api.domain.response

import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import java.time.Instant

data class ArbeidssokerperiodeResponse(
    val startet: Instant,
    val avsluttet: Instant? = null
)

data class ArbeidssokerperiodeResponseV2(
    val startet: Metadata,
    val avsluttet: Metadata? = null,
)
