package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidssokerperioderTable
import no.nav.paw.arbeidssokerregisteret.api.domain.Foedselsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponse
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class ArbeidssokerperiodeRepository(private val database: Database) {
    fun hentArbeidssokerperioderMedFoedselsnummer(foedselsnummer: Foedselsnummer): List<ArbeidssokerperiodeResponse> = transaction(database) {
        ArbeidssokerperioderTable.select { ArbeidssokerperioderTable.foedselsnummer eq foedselsnummer.verdi }
            .map {
                ArbeidssokerperiodeResponse(
                    it[ArbeidssokerperioderTable.startet],
                    it[ArbeidssokerperioderTable.avsluttet]
                )
            }
    }
}
