package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.response.OpplysningerOmArbeidssoekerResponse
import no.nav.paw.arbeidssokerregisteret.api.repositories.OpplysningerOmArbeidssoekerRepository
import no.nav.paw.arbeidssokerregisteret.api.v1.OpplysningerOmArbeidssoeker
import java.util.*

class OpplysningerOmArbeidssoekerService(private val opplysningerOmArbeidssoekerRepository: OpplysningerOmArbeidssoekerRepository) {
    fun hentOpplysningerOmArbeidssoeker(periodeId: UUID): List<OpplysningerOmArbeidssoekerResponse> = opplysningerOmArbeidssoekerRepository.hentOpplysningerOmArbeidssoeker(periodeId)

    fun opprettOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoeker: OpplysningerOmArbeidssoeker) = opplysningerOmArbeidssoekerRepository.opprettOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoeker)
}
