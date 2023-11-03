package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidssokerperioderTable
import no.nav.paw.arbeidssokerregisteret.api.database.BrukerTable
import no.nav.paw.arbeidssokerregisteret.api.database.MetadataTable
import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokerperiodeResponseV2
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

class ArbeidssokerperiodeRepository(private val database: Database) {
    fun hentArbeidssokerperioderMedIdentitetsnummer(identitetsnummer: Identitetsnummer): List<ArbeidssokerperiodeResponse> = transaction(database) {
        ArbeidssokerperioderTable.select { ArbeidssokerperioderTable.identitetsnummer eq identitetsnummer.verdi }
            .map { mapArbeidssokerperiodeResponse(it) }
    }
    fun hentArbeidssokerperioderMedIdentitetsnummerV2(identitetsnummer: Identitetsnummer): List<ArbeidssokerperiodeResponseV2> = transaction(database) {
        (ArbeidssokerperioderTable innerJoin MetadataTable innerJoin BrukerTable).select { ArbeidssokerperioderTable.identitetsnummer eq identitetsnummer.verdi }
            .map {
                ArbeidssokerperiodeResponseV2(
                    Metadata(Instant.now(), Bruker(BrukerType.SLUTTBRUKER, "2"), "test", "test"),
                    null
                )
            }
    }

    fun hentArbeidssokerperiodeMedPeriodeId(id: UUID) = transaction(database) { ArbeidssokerperioderTable.select { ArbeidssokerperioderTable.periodeId eq id }.singleOrNull() }

    fun opprettArbeidssokerperiode(arbeidssokerPeriode: Periode) {
        transaction {
            val startetId = insertMetadata(arbeidssokerPeriode.startet)
            val avsluttetId = arbeidssokerPeriode.avsluttet?.let { insertMetadata(it) }

            insertArbeidssokerperiode(arbeidssokerPeriode.id, arbeidssokerPeriode.identitetsnummer, startetId, avsluttetId)
        }
    }

    fun oppdaterArbeidssokerperiode(arbeidssokerPeriode: Periode) {
        transaction {
            val existingPeriode = ArbeidssokerperioderTable.select { ArbeidssokerperioderTable.periodeId eq arbeidssokerPeriode.id }.singleOrNull()

            existingPeriode?.let {
                val startetId = it[ArbeidssokerperioderTable.startetId]
                val avsluttetId = it[ArbeidssokerperioderTable.avsluttetId]

                updateMetadata(startetId, arbeidssokerPeriode.startet)
                arbeidssokerPeriode.avsluttet?.let { avsluttetMetadata ->
                    updateAvsluttetMetadata(avsluttetId, avsluttetMetadata, arbeidssokerPeriode.id)
                }
            }
        }
    }

    private fun mapArbeidssokerperiodeResponse(row: ResultRow): ArbeidssokerperiodeResponse {
        val startetTidspunkt = fetchTidspunkt(row[ArbeidssokerperioderTable.startetId])
        val avsluttetTidspunkt = row[ArbeidssokerperioderTable.avsluttetId]?.let { fetchTidspunkt(it) }
        return ArbeidssokerperiodeResponse(startetTidspunkt, avsluttetTidspunkt)
    }


    private fun fetchTidspunkt(metadataId: Long): Instant {
        return MetadataTable.select { MetadataTable.id eq metadataId }
            .singleOrNull()
            ?.get(MetadataTable.tidspunkt)
            ?: throw Exception("Metadata tidspunkt ikke funnet")
    }

    private fun insertMetadata(metadata: Metadata): Long {
        return MetadataTable.insertAndGetId {
            it[utfoertAvId] = insertBruker(metadata.utfoertAv)
            it[tidspunkt] = metadata.tidspunkt
            it[kilde] = metadata.kilde
            it[aarsak] = metadata.aarsak
        }.value
    }

    private fun insertBruker(bruker: Bruker): Long {
        val eksisterendeBruker = BrukerTable.select { BrukerTable.brukerId eq bruker.id and (BrukerTable.type eq bruker.type)}.firstOrNull()
        return if(eksisterendeBruker != null){
            eksisterendeBruker[BrukerTable.id].value
        } else {
            BrukerTable.insertAndGetId{
                it[brukerId] = bruker.id
                it[type] = bruker.type
            }.value
        }
    }

    private fun insertArbeidssokerperiode(periodeId: UUID, identitetsnummer: String, startetId: Long, avsluttetId: Long?) {
        ArbeidssokerperioderTable.insert {
            it[ArbeidssokerperioderTable.periodeId] = periodeId
            it[ArbeidssokerperioderTable.identitetsnummer] = identitetsnummer
            it[ArbeidssokerperioderTable.startetId] = startetId
            it[ArbeidssokerperioderTable.avsluttetId] = avsluttetId
        }
    }

    private fun updateMetadata(metadataId: Long, metadata: Metadata) {
        MetadataTable.update({ MetadataTable.id eq metadataId }) {
            it[utfoertAvId] = insertBruker(metadata.utfoertAv)
            it[tidspunkt] = metadata.tidspunkt
            it[kilde] = metadata.kilde
            it[aarsak] = metadata.aarsak
        }
    }

    private fun updateAvsluttetMetadata(avsluttetId: Long?, avsluttetMetadata: Metadata, arbeidssokerPeriodeId: UUID) {
        if (avsluttetId != null) {
            updateMetadata(avsluttetId, avsluttetMetadata)
        } else {
            val newAvsluttetId = insertMetadata(avsluttetMetadata)
            ArbeidssokerperioderTable.update({ ArbeidssokerperioderTable.periodeId eq arbeidssokerPeriodeId }) {
                it[ArbeidssokerperioderTable.avsluttetId] = newAvsluttetId
            }
        }
    }
}
