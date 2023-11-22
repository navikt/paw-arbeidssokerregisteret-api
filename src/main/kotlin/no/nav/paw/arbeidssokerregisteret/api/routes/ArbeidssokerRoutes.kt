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
import no.nav.paw.arbeidssokerregisteret.api.services.PeriodeService
import no.nav.paw.arbeidssokerregisteret.api.services.SituasjonService
import no.nav.paw.arbeidssokerregisteret.api.utils.getNavAnsatt
import no.nav.paw.arbeidssokerregisteret.api.utils.getPidClaim
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import java.util.UUID

fun Route.arbeidssokerRoutes(autorisasjonService: AutorisasjonService, periodeService: PeriodeService, situasjonService: SituasjonService) {
    route("/api/v1") {
        authenticate("tokenx") {
            route("/arbeidssokerperioder") {
                get {
                    logger.info("Henter arbeidssøkerperioder for bruker")

                    val foedselsnummer = call.getPidClaim()

                    val arbeidssoekerperioder = periodeService.hentPerioder(foedselsnummer)

                    logger.info("Hentet arbeidssøkerperioder for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/arbeidssokersituasjon/{periodeId}") {
                get {
                    val periodeId = UUID.fromString(call.parameters["periodeId"])

                    logger.info("Henter arbeidssøkersituasjoner for bruker med periodeId: $periodeId")

                    val arbeidssoekerSituasjon = situasjonService.hentSituasjoner(periodeId)

                    logger.info("Hentet arbeidssøkersituasjoner for bruker med periodeId: $periodeId")

                    call.respond(HttpStatusCode.OK, arbeidssoekerSituasjon)
                }
            }
        }
        authenticate("azure") {
            route("/veileder/arbeidssokerperioder") {
                post {
                    val (identitesnummer) = call.receive<ArbeidssokerperiodeRequest>()

                    val navAnsatt = call.getNavAnsatt()

                    val harNavAnsattTilgangTilBruker = autorisasjonService.verifiserTilgangTilBruker(navAnsatt, identitesnummer)

                    if (!harNavAnsattTilgangTilBruker) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    val arbeidssoekerperioder = periodeService.hentPerioder(identitesnummer)

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
            route("/veileder/arbeidssokersituasjoner") {
                post {
                    val (identitetsnummer, periodeId) = call.receive<ArbeidssokersituasjonRequest>()

                    val navAnsatt = call.getNavAnsatt()

                    val harNavAnsattTilgangTilBruker = autorisasjonService.verifiserTilgangTilBruker(navAnsatt, identitetsnummer)

                    if (!harNavAnsattTilgangTilBruker) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    val arbeidssoekerSituasjon = situasjonService.hentSituasjoner(periodeId)

                    call.respond(HttpStatusCode.OK, arbeidssoekerSituasjon)
                }
            }
        }
    }
}

data class ArbeidssokerperiodeRequest(
    val identitetsnummer: Identitetsnummer
)

data class ArbeidssokersituasjonRequest(
    val identitetsnummer: Identitetsnummer,
    val periodeId: UUID
)
