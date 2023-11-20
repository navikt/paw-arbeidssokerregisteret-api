package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidserfaringTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelseMedDetaljerTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelseTable
import no.nav.paw.arbeidssokerregisteret.api.database.BrukerTable
import no.nav.paw.arbeidssokerregisteret.api.database.DetaljerTable
import no.nav.paw.arbeidssokerregisteret.api.database.HelseTable
import no.nav.paw.arbeidssokerregisteret.api.database.MetadataTable
import no.nav.paw.arbeidssokerregisteret.api.database.SituasjonTable
import no.nav.paw.arbeidssokerregisteret.api.database.UtdanningTable
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidserfaringResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseMedDetaljerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BrukerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BrukerTypeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.JaNeiVetIkkeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.MetadataResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.SituasjonResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningsnivaaResponse
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Helse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanning
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanningsnivaa
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SituasjonConverter {
    fun konverterTilSituasjonResponse(resultRow: ResultRow): SituasjonResponse {
        val periodeId = resultRow[SituasjonTable.periodeId]
        val situasjonId = resultRow[SituasjonTable.id]
        val sendtInnAvMetadata = MetadataTable.select { MetadataTable.utfoertAvId eq resultRow[SituasjonTable.sendtInnAv] }.singleOrNull()
            ?: throw Error("Fant ikke metadata")
        val sendtInnAvBruker = BrukerTable.select { BrukerTable.id eq sendtInnAvMetadata[MetadataTable.utfoertAvId] }.singleOrNull()
            ?: throw Error("Fant ikke bruker")
        val utdanning = UtdanningTable.select { UtdanningTable.id eq resultRow[SituasjonTable.utdanningId] }.singleOrNull()
            ?: throw Error("Fant ikke utdanning")
        val helse = HelseTable.select { HelseTable.id eq resultRow[SituasjonTable.helseId] }.singleOrNull()
            ?: throw Error("Fant ikke helse")
        val arbeidserfaring = ArbeidserfaringTable.select { ArbeidserfaringTable.id eq resultRow[SituasjonTable.arbeidserfaringId] }.singleOrNull()
            ?: throw Error("Fant ikke arbeidserfaring")
        val beskrivelseMedDetaljer = BeskrivelseMedDetaljerTable.select { BeskrivelseMedDetaljerTable.situasjonId eq situasjonId.value }
            .map { beskrivelseMedDetaljerResultRow ->
                val beskrivelse = BeskrivelseTable.select { BeskrivelseTable.beskrivelseMedDetaljerId eq beskrivelseMedDetaljerResultRow[BeskrivelseMedDetaljerTable.id].value }.singleOrNull()
                    ?: throw Error("Fant ikke beskrivelse")
                val detaljer = DetaljerTable.select { DetaljerTable.beskrivelseId eq beskrivelse[BeskrivelseTable.id].value }.associate { detaljerResultRow ->
                    detaljerResultRow[DetaljerTable.noekkel] to detaljerResultRow[DetaljerTable.verdi]
                }
                BeskrivelseMedDetaljerResponse(
                    beskrivelse = BeskrivelseResponse.valueOf(beskrivelse[BeskrivelseTable.beskrivelse].name),
                    detaljer = detaljer
                )
            }

        return SituasjonResponse(
            periodeId = periodeId,
            sendtInnAv = MetadataResponse(
                tidspunkt = sendtInnAvMetadata[MetadataTable.tidspunkt],
                utfoertAv = BrukerResponse(
                    type = BrukerTypeResponse.valueOf(sendtInnAvBruker[BrukerTable.type].name)
                ),
                kilde = sendtInnAvMetadata[MetadataTable.kilde],
                aarsak = sendtInnAvMetadata[MetadataTable.aarsak]
            ),
            utdanning = UtdanningResponse(
                lengde = UtdanningsnivaaResponse.valueOf(utdanning[UtdanningTable.lengde].name),
                bestaatt = JaNeiVetIkkeResponse.valueOf(utdanning[UtdanningTable.bestaatt].name),
                godkjent = JaNeiVetIkkeResponse.valueOf(utdanning[UtdanningTable.godkjent].name)
            ),
            helse = JaNeiVetIkkeResponse.valueOf(helse[HelseTable.helsetilstandHindrerArbeid].name),
            arbeidserfaring = ArbeidserfaringResponse(
                harHattArbeid = JaNeiVetIkkeResponse.valueOf(arbeidserfaring[ArbeidserfaringTable.harHattArbeid].name)
            ),
            arbeidssokersituasjon = beskrivelseMedDetaljer
        )
    }
}

class SituasjonRepository(private val database: Database) {
    fun hentSituasjon(periodeId: UUID): SituasjonResponse? =
        transaction(database) {
            SituasjonTable.select { SituasjonTable.periodeId eq periodeId }.singleOrNull()?.let { resultRow ->
                SituasjonConverter().konverterTilSituasjonResponse(resultRow)
            }
        }

    fun hentSituasjoner(periodeId: UUID): List<SituasjonResponse> =
        transaction(database) {
            SituasjonTable.select {
                SituasjonTable.periodeId eq periodeId
            }.map { resultRow ->
                SituasjonConverter().konverterTilSituasjonResponse(resultRow)
            }
        }

    fun opprettSituasjon(situasjon: Situasjon) {
        transaction(database) {
            try {
                val sendtInnAvId = insertMetadata(situasjon.sendtInnAv)
                val utdanningId = insertUtdanning(situasjon.utdanning)
                val helseId = insertHelse(situasjon.helse)
                val arbeidserfaringId = insertArbeidserfaring(situasjon.arbeidserfaring)
                val situasjonId = insertSituasjon(situasjon, sendtInnAvId, utdanningId, helseId, arbeidserfaringId)

                situasjon.arbeidsoekersituasjon.beskrivelser.forEach { beskrivelseMedDetaljer ->
                    val beskrivelseMedDetaljerId = insertBeskrivelseMedDetaljer(situasjonId)
                    val beskrivelserId = insertBeskrivelse(beskrivelseMedDetaljer.beskrivelse, beskrivelseMedDetaljerId)
                    beskrivelseMedDetaljer.detaljer.forEach { detalj ->
                        insertDetalj(beskrivelserId, detalj)
                    }
                }
            } catch (e: Exception) {
                logger.error("Feil ved opprettelse av situasjonsbesvarelse", e)
            }
        }
    }

    private fun insertMetadata(metadata: Metadata): Long =
        MetadataTable.insertAndGetId {
            it[tidspunkt] = metadata.tidspunkt
            it[utfoertAvId] = insertBruker(metadata.utfoertAv)
            it[kilde] = metadata.kilde
            it[aarsak] = metadata.aarsak
        }.value

    private fun insertBruker(bruker: Bruker): Long =
        BrukerTable.insertAndGetId {
            it[type] = BrukerType.valueOf(bruker.type.name)
            it[brukerId] = bruker.id
        }.value

    private fun insertUtdanning(utdanning: Utdanning): Long =
        UtdanningTable.insertAndGetId {
            it[lengde] = Utdanningsnivaa.valueOf(utdanning.lengde.name)
            it[bestaatt] = JaNeiVetIkke.valueOf(utdanning.bestaatt.name)
            it[godkjent] = JaNeiVetIkke.valueOf(utdanning.godkjent.name)
        }.value

    private fun insertHelse(helse: Helse): Long =
        HelseTable.insertAndGetId {
            it[helsetilstandHindrerArbeid] = JaNeiVetIkke.valueOf(helse.helsetilstandHindrerArbeid.name)
        }.value

    private fun insertArbeidserfaring(arbeidserfaring: Arbeidserfaring): Long =
        ArbeidserfaringTable.insertAndGetId {
            it[harHattArbeid] = JaNeiVetIkke.valueOf(arbeidserfaring.harHattArbeid.name)
        }.value

    private fun insertSituasjon(
        situasjon: Situasjon,
        sendtInnAvId: Long,
        utdanningId: Long,
        helseId: Long,
        arbeidserfaringId: Long
    ): Long =
        SituasjonTable.insertAndGetId {
            it[periodeId] = situasjon.periodeId
            it[sendtInnAv] = sendtInnAvId
            it[SituasjonTable.utdanningId] = utdanningId
            it[SituasjonTable.helseId] = helseId
            it[SituasjonTable.arbeidserfaringId] = arbeidserfaringId
        }.value

    private fun insertBeskrivelseMedDetaljer(situasjonId: Long): Long =
        BeskrivelseMedDetaljerTable.insertAndGetId {
            it[BeskrivelseMedDetaljerTable.situasjonId] = situasjonId
        }.value

    private fun insertBeskrivelse(beskrivelse: Beskrivelse, beskrivelseMedDetaljerId: Long): Long =
        BeskrivelseTable.insertAndGetId {
            it[BeskrivelseTable.beskrivelse] = Beskrivelse.valueOf(beskrivelse.name)
            it[BeskrivelseTable.beskrivelseMedDetaljerId] = beskrivelseMedDetaljerId
        }.value

    private fun insertDetalj(beskrivelseId: Long, detalj: Map.Entry<String, String>) {
        DetaljerTable.insert {
            it[DetaljerTable.beskrivelseId] = beskrivelseId
            it[noekkel] = detalj.key
            it[verdi] = detalj.value
        }
    }
}
