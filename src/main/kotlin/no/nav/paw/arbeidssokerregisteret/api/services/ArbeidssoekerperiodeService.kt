package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssoekerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.ArbeidssoekerperiodeRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode

class ArbeidssoekerperiodeService(private val arbeidssoekerperiodeRepository: ArbeidssoekerperiodeRepository) {
    fun hentArbeidssoekerperioder(identitetsnummer: Identitetsnummer): List<ArbeidssoekerperiodeResponse> = arbeidssoekerperiodeRepository.hentArbeidssoekerperioder(identitetsnummer)

    fun opprettEllerOppdaterArbeidssoekerperiode(arbeidssoekerperiode: Periode) {
        val periode = arbeidssoekerperiodeRepository.hentArbeidssoekerperiodeMedPeriodeId(arbeidssoekerperiode.id)
        if (periode != null) {
            arbeidssoekerperiodeRepository.oppdaterArbeidssoekerperiode(arbeidssoekerperiode)
        } else {
            arbeidssoekerperiodeRepository.opprettArbeidssoekerperiode(arbeidssoekerperiode)
        }
    }
}
