package no.nav.paw.arbeidssokerregisteret.api.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authentication
import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.toIdentitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.plugins.StatusException
import no.nav.paw.arbeidssokerregisteret.api.services.NavAnsatt
import no.nav.security.token.support.v2.TokenValidationContextPrincipal

fun ApplicationCall.getClaim(
    issuer: String,
    name: String
): String? =
    authentication.principal<TokenValidationContextPrincipal>()
        ?.context
        ?.getClaims(issuer)
        ?.getStringClaim(name)

fun ApplicationCall.getPidClaim(): Identitetsnummer =
    getClaim("tokenx", "pid")?.toIdentitetsnummer()
        ?: throw StatusException(HttpStatusCode.Forbidden, "Fant ikke 'pid'-claim i token fra tokenx-issuer")

private fun ApplicationCall.getNavAnsattAzureId(): String =
    getClaim("azure", "oid") ?: throw StatusException(HttpStatusCode.Forbidden, "Fant ikke 'oid'-claim i token fra azure-issuer")

private fun ApplicationCall.getNavAnsattIdent(): String =
    getClaim("azure", "NAVident") ?: throw StatusException(HttpStatusCode.Forbidden, "Fant ikke 'NAVident'-claim i token fra azure-issuer")

fun ApplicationCall.getNavAnsatt(): NavAnsatt =
    NavAnsatt(
        getNavAnsattAzureId(),
        getNavAnsattIdent()
    )
