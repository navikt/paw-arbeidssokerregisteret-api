package no.nav.paw.arbeidssokerregisteret.api.services

import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.utils.auditLogMelding
import no.nav.paw.arbeidssokerregisteret.api.utils.auditLogger
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.poao_tilgang.client.NavAnsattNavIdentLesetilgangTilEksternBrukerPolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangCachedClient

class AutorisasjonService(
    val poaoTilgangHttpClient: PoaoTilgangCachedClient
) {
    fun verifiserTilgangTilBruker(
        navAnsatt: NavAnsatt,
        identitetsnummer: Identitetsnummer
    ): Boolean {
        val harNavAnsattTilgang =
            poaoTilgangHttpClient.evaluatePolicy(
                NavAnsattNavIdentLesetilgangTilEksternBrukerPolicyInput(navAnsatt.azureId, identitetsnummer.verdi)
            ).getOrThrow().isPermit

        if (!harNavAnsattTilgang) {
            logger.info("NAV-ansatt har ikke tilgang til bruker")
        } else {
            auditLogger.info(auditLogMelding(identitetsnummer, navAnsatt, "NAV ansatt har hentet informasjon om bruker"))
        }
        return harNavAnsattTilgang
    }
}

data class NavAnsatt(val azureId: String, val navIdent: String)
