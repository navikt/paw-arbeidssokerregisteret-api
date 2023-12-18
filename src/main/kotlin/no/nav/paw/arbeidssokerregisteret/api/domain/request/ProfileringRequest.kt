package no.nav.paw.arbeidssokerregisteret.api.domain.request

import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import java.util.*

data class ProfileringRequest(
    val identitetsnummer: Identitetsnummer,
    val periodeId: UUID
)
