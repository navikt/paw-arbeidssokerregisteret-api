package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssokerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode

class ArbeidssokerperiodeService(private val arbeidssokerperiodeRepository: ArbeidssokerperiodeRepository) {
    fun hentArbeidssokerperioder(identitetsnummer: Identitetsnummer): List<ArbeidssokerperiodeResponse> = arbeidssokerperiodeRepository.hentArbeidssokerperioder(identitetsnummer)

    fun opprettEllerOppdaterArbeidssokerperiode(arbeidssokerperiode: Periode) {
        val periode = arbeidssokerperiodeRepository.hentArbeidssokerperiodeMedPeriodeId(arbeidssokerperiode.id)
        if (periode != null) {
            arbeidssokerperiodeRepository.oppdaterArbeidssokerperiode(arbeidssokerperiode)
        } else {
            arbeidssokerperiodeRepository.opprettArbeidssokerperiode(arbeidssokerperiode)
        }
    }
}
