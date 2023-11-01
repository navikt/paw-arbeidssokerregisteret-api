package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Arbeidssokerperiode
import no.nav.paw.arbeidssokerregisteret.api.domain.Foedselsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssokerperiodeRepository

class ArbeidssokerperiodeService(private val arbeidssokerperiodeRepository: ArbeidssokerperiodeRepository) {
    fun hentArbeidssokerperioder(foedselsnummer: Foedselsnummer): List<ArbeidssokerperiodeResponse> =
        arbeidssokerperiodeRepository.hentArbeidssokerperioderMedFoedselsnummer(foedselsnummer)

    fun opprettArbeidssokerperiode(arbeidssokerperiode: Arbeidssokerperiode) {
        TODO()
    }
}
