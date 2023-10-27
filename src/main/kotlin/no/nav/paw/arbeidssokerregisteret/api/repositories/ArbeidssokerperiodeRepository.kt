package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidssokerperioderTable
import no.nav.paw.arbeidssokerregisteret.api.domain.Arbeidssokerperiode
import no.nav.paw.arbeidssokerregisteret.api.domain.Foedselsnummer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class ArbeidssokerperiodeRepository(private val database: Database) {
    fun hentArbeidssokerperioderMedFoedselsnummer(foedselsnummer: Foedselsnummer): List<Arbeidssokerperiode> = transaction(database) {
        ArbeidssokerperioderTable.select { ArbeidssokerperioderTable.foedselsnummer eq foedselsnummer.verdi }
            .map {
                Arbeidssokerperiode(
                    it[ArbeidssokerperioderTable.startet],
                    it[ArbeidssokerperioderTable.avsluttet]
                )
            }
    }
}
