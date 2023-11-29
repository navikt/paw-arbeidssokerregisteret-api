package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.PeriodeResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.PeriodeRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode

class PeriodeService(private val periodeRepository: PeriodeRepository) {
    fun hentPerioder(identitetsnummer: Identitetsnummer): List<PeriodeResponse> = periodeRepository.hentPerioder(identitetsnummer)

    fun opprettEllerOppdaterPeriode(periode: Periode) {
        val eksisterendePeriode = periodeRepository.hentPeriode(periode.id)
        if (eksisterendePeriode != null) {
            periodeRepository.oppdaterPeriode(periode)
        } else {
            periodeRepository.opprettPeriode(periode)
        }
    }
}
