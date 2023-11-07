package no.nav.paw.arbeidssokerregisteret.api.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssokerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.getPidClaim
import no.nav.paw.arbeidssokerregisteret.api.utils.logger

fun Route.arbeidssokerRoutes(arbeidssokerperiodeService: ArbeidssokerperiodeService) {
    route("/api/v1") {
        authenticate("tokenx", "azure") {
            route("/arbeidssokerperioder") {
                get {
                    logger.info("Henter arbeidssøkerperiode for bruker")

                    val foedselsnummer = call.getPidClaim()

                    val arbeidssokerperioder = arbeidssokerperiodeService.hentArbeidssokerperioder(foedselsnummer)
                    val arbeidssokerperioderV2 = arbeidssokerperiodeService.hentArbeidssokerperioderV2(foedselsnummer)

                    logger.info("{}", arbeidssokerperioderV2)

                    logger.info("Hentet arbeidssøkerperiode for bruker")

                    call.respond(HttpStatusCode.OK, arbeidssokerperioderV2)
                }
            }
        }
    }
}
