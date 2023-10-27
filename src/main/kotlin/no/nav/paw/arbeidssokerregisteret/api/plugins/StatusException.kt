package no.nav.paw.arbeidssokerregisteret.api.plugins

import io.ktor.http.HttpStatusCode

open class StatusException(val status: HttpStatusCode, val description: String? = null) :
    Exception("Request failed with status: $status. Description: $description")
