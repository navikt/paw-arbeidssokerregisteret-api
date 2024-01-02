package no.nav.paw.arbeidssokerregisteret.api.domain.response

import java.util.*

enum class UtdanningsnivaaResponse {
    UKJENT_VERDI,
    UDEFINERT,
    INGEN_UTDANNING,
    GRUNNSKOLE,
    VIDEREGAENDE_GRUNNUTDANNING,
    VIDEREGAENDE_FAGBREV_SVENNEBREV,
    HOYERE_UTDANNING_1_TIL_4,
    HOYERE_UTDANNING_5_ELLER_MER
}

enum class JaNeiVetIkkeResponse {
    JA,
    NEI,
    VET_IKKE
}

enum class BeskrivelseResponse {
    UKJENT_VERDI,
    UDEFINERT,
    HAR_SAGT_OPP,
    HAR_BLITT_SAGT_OPP,
    ER_PERMITTERT,
    ALDRI_HATT_JOBB,
    IKKE_VAERT_I_JOBB_SISTE_2_AAR,
    AKKURAT_FULLFORT_UTDANNING,
    VIL_BYTTE_JOBB,
    USIKKER_JOBBSITUASJON,
    MIDLERTIDIG_JOBB,
    DELTIDSJOBB_VIL_MER,
    NY_JOBB,
    KONKURS,
    ANNET
}

data class OpplysningerOmArbeidssoekerResponse(
    val opplysningerOmArbeidssoekerId: UUID,
    val periodeId: UUID,
    val sendtInnAv: MetadataResponse,
    val utdanning: UtdanningResponse,
    val helse: HelseResponse,
    val arbeidserfaring: ArbeidserfaringResponse,
    val annet: AnnetResponse,
    val jobbsituasjon: List<BeskrivelseMedDetaljerResponse>
)

data class ArbeidserfaringResponse(
    val harHattArbeid: JaNeiVetIkkeResponse
)

data class HelseResponse(
    val helseTilstandHindrerArbeid: JaNeiVetIkkeResponse
)

data class UtdanningResponse(
    val lengde: UtdanningsnivaaResponse,
    val bestaatt: JaNeiVetIkkeResponse,
    val godkjent: JaNeiVetIkkeResponse
)

data class AnnetResponse(
    val andreForholdHindrerArbeid: JaNeiVetIkkeResponse
)

data class BeskrivelseMedDetaljerResponse(
    val beskrivelse: BeskrivelseResponse,
    val detaljer: Map<String, String>
)
