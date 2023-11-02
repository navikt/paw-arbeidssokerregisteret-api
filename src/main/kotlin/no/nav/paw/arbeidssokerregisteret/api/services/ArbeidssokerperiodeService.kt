package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Foedselsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssokerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode

class ArbeidssokerperiodeService(private val arbeidssokerperiodeRepository: ArbeidssokerperiodeRepository) {
    fun hentArbeidssokerperioder(foedselsnummer: Foedselsnummer): List<ArbeidssokerperiodeResponse> =
        arbeidssokerperiodeRepository.hentArbeidssokerperioderMedFoedselsnummer(foedselsnummer)

    fun opprettEllerOppdaterArbeidssokerperiode(arbeidssokerperiode: Periode) {
        val periode = arbeidssokerperiodeRepository.hentArbeidssokerperiodeMedId(arbeidssokerperiode.id)
        if(periode != null){
            arbeidssokerperiodeRepository.oppdaterArbeidssokerperiode(arbeidssokerperiode)
        } else {
            arbeidssokerperiodeRepository.opprettArbeidssokerperiode(arbeidssokerperiode)
        }
    }
}
