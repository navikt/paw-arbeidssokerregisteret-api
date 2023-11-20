package no.nav.paw.arbeidssokerregisteret.api.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssoekerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.getPidClaim
import no.nav.paw.arbeidssokerregisteret.api.utils.logger

fun Route.arbeidssokerRoutes(arbeidssoekerperiodeService: ArbeidssoekerperiodeService) {
    route("/api/v1") {
        authenticate("tokenx", "azure") {
            route("/arbeidssokerperioder") {
                get {
                    logger.info("Henter arbeidssøkerperioder for bruker")

                    val foedselsnummer = call.getPidClaim()

                    val arbeidssoekerperioder = arbeidssoekerperiodeService.hentArbeidssoekerperioder(foedselsnummer)

                    logger.info("Hentet arbeidssøkerperioder for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssoekerperioder)
                }
            }
        }
    }
}
