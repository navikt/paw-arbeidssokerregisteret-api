package no.nav.paw.arbeidssokerregisteret.api.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.services.AutorisasjonService
import no.nav.paw.arbeidssokerregisteret.api.services.OpplysningerOmArbeidssoekerService
import no.nav.paw.arbeidssokerregisteret.api.services.PeriodeService
import no.nav.paw.arbeidssokerregisteret.api.services.ProfileringService
import no.nav.paw.arbeidssokerregisteret.api.utils.getNavAnsatt
import no.nav.paw.arbeidssokerregisteret.api.utils.getPidClaim
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import java.util.UUID

fun Route.arbeidssokerRoutes(
    autorisasjonService: AutorisasjonService,
    periodeService: PeriodeService,
    opplysningerOmArbeidssoekerService: OpplysningerOmArbeidssoekerService,
    profileringService: ProfileringService
) {
    route("/api/v1") {
        authenticate("tokenx") {
            route("/arbeidssoekerperioder") {
                get {
                    logger.info("Henter arbeidssøkerperioder for bruker")

                    val foedselsnummer = call.getPidClaim()

                    val arbeidssoekerperioder = periodeService.hentPerioder(foedselsnummer)

                    logger.info("Hentet arbeidssøkerperioder for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/opplysninger-om-arbeidssoeker/{periodeId}") {
                get {
                    val periodeId = UUID.fromString(call.parameters["periodeId"])

                    logger.info("Henter opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    val opplysningerOmArbeidssoeker = opplysningerOmArbeidssoekerService.hentOpplysningerOmArbeidssoeker(periodeId)

                    logger.info("Hentet opplysninger-om-arbeidssøker for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, opplysningerOmArbeidssoeker)
                }
            }
            route("/profilering/{periodeId}") {
                get {
                    val periodeId = UUID.fromString(call.parameters["periodeId"])

                    logger.info("Henter profilering for bruker med periodeId: $periodeId")

                    val profilering = profileringService.hentProfileringForArbeidssoekerMedPeriodeId(periodeId)

                    logger.info("Hentet profilering for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, profilering)
                }
            }
            route("/profilering/{opplysningerOmArbeidssoekerId}") {
                get {
                    val opplysningerOmArbeidssoekerId = UUID.fromString(call.parameters["opplysningerOmArbeidssoekerId"])

                    logger.info("Henter profilering for bruker med opplysningerOmArbeidssoekerId: $opplysningerOmArbeidssoekerId")

                    val profilering = profileringService.hentProfileringForArbeidssoekerMedOpplysningerOmArbeidssoekerId(opplysningerOmArbeidssoekerId)

                    logger.info("Hentet profilering for bruker med opplysningerOmArbeidssoekerId: $opplysningerOmArbeidssoekerId")

                    call.respond(HttpStatusCode.OK, profilering)
                }
            }
        }
        authenticate("azure") {
            route("/veileder/arbeidssoekerperioder") {
                post {
                    val (identitesnummer) = call.receive<ArbeidssokerperiodeRequest>()

                    val navAnsatt = call.getNavAnsatt()

                    val harNavAnsattTilgangTilBruker = autorisasjonService.verifiserTilgangTilBruker(navAnsatt, Identitetsnummer(identitesnummer))

                    if (!harNavAnsattTilgangTilBruker) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    val arbeidssoekerperioder = periodeService.hentPerioder(Identitetsnummer(identitesnummer))

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/veileder/opplysninger-om-arbeidssoeker") {
                post {
                    val (identitetsnummer, periodeId) = call.receive<OpplysningerOmArbeidssoekerRequest>()

                    val navAnsatt = call.getNavAnsatt()

                    val harNavAnsattTilgangTilBruker = autorisasjonService.verifiserTilgangTilBruker(navAnsatt, identitetsnummer)

                    if (!harNavAnsattTilgangTilBruker) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    val opplysningerOmArbeidssoeker = opplysningerOmArbeidssoekerService.hentOpplysningerOmArbeidssoeker(periodeId)

                    call.respond(HttpStatusCode.OK, opplysningerOmArbeidssoeker)
                }
            }
            route("/veileder/profilering") {
                post {
                    val (identitetsnummer, periodeId) = call.receive<ProfileringRequest>()

                    val navAnsatt = call.getNavAnsatt()

                    val harNavAnsattTilgangTilBruker = autorisasjonService.verifiserTilgangTilBruker(navAnsatt, identitetsnummer)

                    if (!harNavAnsattTilgangTilBruker) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    val profilering = profileringService.hentProfileringForArbeidssoekerMedPeriodeId(periodeId)

                    call.respond(HttpStatusCode.OK, profilering)
                }
            }
        }
    }
}

data class ArbeidssokerperiodeRequest(
    val identitetsnummer: String
)

data class OpplysningerOmArbeidssoekerRequest(
    val identitetsnummer: Identitetsnummer,
    val periodeId: UUID
)

data class ProfileringRequest(
    val identitetsnummer: Identitetsnummer,
    val periodeId: UUID
)
