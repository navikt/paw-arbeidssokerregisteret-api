package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.response.SituasjonResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.SituasjonRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import java.util.*

class SituasjonService(private val situasjonRepository: SituasjonRepository) {
    fun hentSituasjoner(periodeId: UUID): List<SituasjonResponse> = situasjonRepository.hentSituasjoner(periodeId)

    fun opprettSituasjon(situasjon: Situasjon) = situasjonRepository.opprettSituasjon(situasjon)
}
