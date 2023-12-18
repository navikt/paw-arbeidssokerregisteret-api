package no.nav.paw.arbeidssokerregisteret.api.domain.request

import java.util.UUID

data class ProfileringRequest(
    val identitetsnummer: String,
    val periodeId: UUID
)
