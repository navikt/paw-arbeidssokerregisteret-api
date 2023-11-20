package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidssoekerperioderTable
import no.nav.paw.arbeidssokerregisteret.api.database.BrukerTable
import no.nav.paw.arbeidssokerregisteret.api.database.MetadataTable
import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssoekerperiodeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.toMetadataResponse
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class PeriodeConverter(private val repository: ArbeidssoekerperiodeRepository) {
    fun convertToPeriode(resultRow: ResultRow): Periode {
        val periodeId = resultRow[ArbeidssoekerperioderTable.periodeId]
        val identitetsnummer = resultRow[ArbeidssoekerperioderTable.identitetsnummer]
        val startetId = resultRow[ArbeidssoekerperioderTable.startetId]
        val avsluttetId = resultRow[ArbeidssoekerperioderTable.avsluttetId]

        val startetMetadata = repository.fetchMetadata(startetId) ?: throw Error("Fant ikke startet metadata")
        val avsluttetMetadata = avsluttetId?.let { repository.fetchMetadata(it) }

        return Periode(
            periodeId,
            identitetsnummer,
            startetMetadata,
            avsluttetMetadata
        )
    }
}

class ArbeidssoekerperiodeRepository(private val database: Database) {
    fun hentArbeidssoekerperiodeMedPeriodeId(periodeId: UUID): Periode? =
        transaction(database) {
            ArbeidssoekerperioderTable.select { ArbeidssoekerperioderTable.periodeId eq periodeId }.singleOrNull()?.let { resultRow ->
                PeriodeConverter(this@ArbeidssoekerperiodeRepository).convertToPeriode(resultRow)
            }
        }

    fun hentArbeidssoekerperioder(identitetsnummer: Identitetsnummer): List<ArbeidssoekerperiodeResponse> =
        transaction(database) {
            ArbeidssoekerperioderTable.select {
                ArbeidssoekerperioderTable.identitetsnummer eq identitetsnummer.verdi
            }.map { row ->
                val startetId = row[ArbeidssoekerperioderTable.startetId]
                val avsluttetId = row[ArbeidssoekerperioderTable.avsluttetId]

                val startetMetadata = fetchMetadata(startetId) ?: throw Error("Fant ikke startet metadata")
                val avsluttetMetadata = avsluttetId?.let { fetchMetadata(it) }

                ArbeidssoekerperiodeResponse(startetMetadata.toMetadataResponse(), avsluttetMetadata?.toMetadataResponse())
            }
        }

    fun fetchMetadata(id: Long): Metadata? {
        return MetadataTable.select { MetadataTable.id eq id }.singleOrNull()?.let { metadata ->
            val brukerId = metadata[MetadataTable.utfoertAvId]
            val bruker = fetchBruker(brukerId)
            Metadata(
                metadata[MetadataTable.tidspunkt],
                bruker,
                metadata[MetadataTable.kilde],
                metadata[MetadataTable.aarsak]
            )
        }
    }

    private fun fetchBruker(brukerId: Long): Bruker? {
        return BrukerTable.select { BrukerTable.id eq brukerId }.singleOrNull()?.let {
            Bruker(
                it[BrukerTable.type],
                it[BrukerTable.brukerId]
            )
        }
    }

    fun opprettArbeidssoekerperiode(arbeidssoekerPeriode: Periode) {
        transaction {
            val startetId = insertMetadata(arbeidssoekerPeriode.startet)
            val avsluttetId = arbeidssoekerPeriode.avsluttet?.let { insertMetadata(it) }

            insertArbeidssoekerperiode(arbeidssoekerPeriode.id, arbeidssoekerPeriode.identitetsnummer, startetId, avsluttetId)
        }
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
        val eksisterendeBruker = BrukerTable.select { BrukerTable.brukerId eq bruker.id and (BrukerTable.type eq bruker.type) }.singleOrNull()
        return if (eksisterendeBruker != null) {
            eksisterendeBruker[BrukerTable.id].value
        } else {
            BrukerTable.insertAndGetId {
                it[brukerId] = bruker.id
                it[type] = bruker.type
            }.value
        }
    }

    private fun insertArbeidssoekerperiode(
        periodeId: UUID,
        identitetsnummer: String,
        startetId: Long,
        avsluttetId: Long?
    ) {
        ArbeidssoekerperioderTable.insert {
            it[ArbeidssoekerperioderTable.periodeId] = periodeId
            it[ArbeidssoekerperioderTable.identitetsnummer] = identitetsnummer
            it[ArbeidssoekerperioderTable.startetId] = startetId
            it[ArbeidssoekerperioderTable.avsluttetId] = avsluttetId
        }
    }

    fun oppdaterArbeidssoekerperiode(arbeidssoekerPeriode: Periode) {
        transaction {
            val eksisterendePeriode = ArbeidssoekerperioderTable.select { ArbeidssoekerperioderTable.periodeId eq arbeidssoekerPeriode.id }.singleOrNull()

            eksisterendePeriode?.let {
                val startetId = it[ArbeidssoekerperioderTable.startetId]
                val avsluttetId = it[ArbeidssoekerperioderTable.avsluttetId]

                oppdaterMetadata(startetId, arbeidssoekerPeriode.startet)
                arbeidssoekerPeriode.avsluttet?.let { avsluttetPeriode -> oppdaterAvsluttetMetadata(avsluttetId, avsluttetPeriode, arbeidssoekerPeriode.id) }
            }
        }
    }

    private fun oppdaterMetadata(
        metadataId: Long,
        metadata: Metadata
    ) {
        MetadataTable.update({ MetadataTable.id eq metadataId }) {
            it[utfoertAvId] = insertBruker(metadata.utfoertAv)
            it[tidspunkt] = metadata.tidspunkt
            it[kilde] = metadata.kilde
            it[aarsak] = metadata.aarsak
        }
    }

    private fun oppdaterAvsluttetMetadata(
        avsluttetId: Long?,
        avsluttetMetadata: Metadata,
        arbeidssoekerPeriodeId: UUID
    ) {
        if (avsluttetId != null) {
            oppdaterMetadata(avsluttetId, avsluttetMetadata)
        } else {
            val avsluttetMetadataId = insertMetadata(avsluttetMetadata)
            ArbeidssoekerperioderTable.update({ ArbeidssoekerperioderTable.periodeId eq arbeidssoekerPeriodeId }) {
                it[ArbeidssoekerperioderTable.avsluttetId] = avsluttetMetadataId
            }
        }
    }
}
