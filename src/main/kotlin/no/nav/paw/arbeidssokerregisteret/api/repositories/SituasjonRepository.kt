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
import no.nav.paw.arbeidssokerregisteret.api.domain.response.HelseResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.JaNeiVetIkkeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.MetadataResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.SituasjonResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningsnivaaResponse
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.Helse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
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

class SituasjonRepository(private val database: Database) {

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
                val sendtInnAvId = PeriodeRepository(database).settInnMetadata(situasjon.sendtInnAv)
                val utdanningId = settInnUtdanning(situasjon.utdanning)
                val helseId = settInnHelse(situasjon.helse)
                val arbeidserfaringId = settInnArbeidserfaring(situasjon.arbeidserfaring)
                val situasjonId = settInnSituasjon(situasjon, sendtInnAvId, utdanningId, helseId, arbeidserfaringId)

                situasjon.arbeidsoekersituasjon.beskrivelser.forEach { beskrivelseMedDetaljer ->
                    val beskrivelseMedDetaljerId = settInnBeskrivelseMedDetaljer(situasjonId)
                    val beskrivelserId = settInnBeskrivelse(beskrivelseMedDetaljer.beskrivelse, beskrivelseMedDetaljerId)
                    beskrivelseMedDetaljer.detaljer.forEach { detalj ->
                        settInnDetaljer(beskrivelserId, detalj)
                    }
                }
            } catch (e: Exception) {
                logger.error("Feil ved opprettelse av situasjon", e)
            }
        }
    }

    private fun settInnUtdanning(utdanning: Utdanning): Long =
        UtdanningTable.insertAndGetId {
            it[lengde] = Utdanningsnivaa.valueOf(utdanning.lengde.name)
            it[bestaatt] = JaNeiVetIkke.valueOf(utdanning.bestaatt.name)
            it[godkjent] = JaNeiVetIkke.valueOf(utdanning.godkjent.name)
        }.value

    private fun settInnHelse(helse: Helse): Long =
        HelseTable.insertAndGetId {
            it[helsetilstandHindrerArbeid] = JaNeiVetIkke.valueOf(helse.helsetilstandHindrerArbeid.name)
        }.value

    private fun settInnArbeidserfaring(arbeidserfaring: Arbeidserfaring): Long =
        ArbeidserfaringTable.insertAndGetId {
            it[harHattArbeid] = JaNeiVetIkke.valueOf(arbeidserfaring.harHattArbeid.name)
        }.value

    private fun settInnSituasjon(
        situasjon: Situasjon,
        sendtInnAvId: Long,
        utdanningId: Long,
        helseId: Long,
        arbeidserfaringId: Long
    ): Long =
        SituasjonTable.insertAndGetId {
            it[situasjonId] = situasjon.id
            it[periodeId] = situasjon.periodeId
            it[SituasjonTable.sendtInnAvId] = sendtInnAvId
            it[SituasjonTable.utdanningId] = utdanningId
            it[SituasjonTable.helseId] = helseId
            it[SituasjonTable.arbeidserfaringId] = arbeidserfaringId
        }.value

    private fun settInnBeskrivelseMedDetaljer(situasjonId: Long): Long =
        BeskrivelseMedDetaljerTable.insertAndGetId {
            it[BeskrivelseMedDetaljerTable.situasjonId] = situasjonId
        }.value

    private fun settInnBeskrivelse(beskrivelse: Beskrivelse, beskrivelseMedDetaljerId: Long): Long =
        BeskrivelseTable.insertAndGetId {
            it[BeskrivelseTable.beskrivelse] = Beskrivelse.valueOf(beskrivelse.name)
            it[BeskrivelseTable.beskrivelseMedDetaljerId] = beskrivelseMedDetaljerId
        }.value

    private fun settInnDetaljer(beskrivelseId: Long, detalj: Map.Entry<String, String>) {
        DetaljerTable.insert {
            it[DetaljerTable.beskrivelseId] = beskrivelseId
            it[noekkel] = detalj.key
            it[verdi] = detalj.value
        }
    }
}

class SituasjonConverter {
    fun konverterTilSituasjonResponse(resultRow: ResultRow): SituasjonResponse {
        val periodeId = resultRow[SituasjonTable.periodeId]
        val situasjonIdPK = resultRow[SituasjonTable.id]
        val situasjonId = resultRow[SituasjonTable.situasjonId]
        val sendtInnAvId = resultRow[SituasjonTable.sendtInnAvId]
        val utdanningId = resultRow[SituasjonTable.utdanningId]

        val sendtInnAvMetadata = hentMetadataResponse(sendtInnAvId)
        val utdanning = hentUtdanningResponse(utdanningId)
        val helse = hentHelseResponse(resultRow[SituasjonTable.helseId])
        val arbeidserfaring = hentArbeidserfaringResponse(resultRow[SituasjonTable.arbeidserfaringId])
        val beskrivelseMedDetaljer = hentBeskrivelseMedDetaljerResponse(situasjonIdPK.value)

        return SituasjonResponse(
            situasjonId = situasjonId,
            periodeId = periodeId,
            sendtInnAv = sendtInnAvMetadata,
            utdanning = utdanning,
            helse = helse,
            arbeidserfaring = arbeidserfaring,
            arbeidssokersituasjon = beskrivelseMedDetaljer
        )
    }

    private fun hentMetadataResponse(metadataId: Long): MetadataResponse {
        return MetadataTable.select { MetadataTable.id eq metadataId }
            .singleOrNull()?.let { metadataResultRow ->
            val utfoertAvId = metadataResultRow[MetadataTable.utfoertAvId]
            val bruker = BrukerTable.select { BrukerTable.id eq utfoertAvId }
                .singleOrNull()?.let { brukerResultRow ->
                BrukerResponse(
                    type = BrukerTypeResponse.valueOf(brukerResultRow[BrukerTable.type].name)
                )
            } ?: throw RuntimeException("Fant ikke bruker: $utfoertAvId")

            MetadataResponse(
                tidspunkt = metadataResultRow[MetadataTable.tidspunkt],
                utfoertAv = bruker,
                kilde = metadataResultRow[MetadataTable.kilde],
                aarsak = metadataResultRow[MetadataTable.aarsak]
            )
        } ?: throw RuntimeException("Fant ikke metadata $metadataId")
    }

    private fun hentUtdanningResponse(utdanningId: Long): UtdanningResponse {
        return UtdanningTable.select { UtdanningTable.id eq utdanningId }
            .singleOrNull()?.let { utdanningResultRow ->
            UtdanningResponse(
                lengde = UtdanningsnivaaResponse.valueOf(utdanningResultRow[UtdanningTable.lengde].name),
                bestaatt = JaNeiVetIkkeResponse.valueOf(utdanningResultRow[UtdanningTable.bestaatt].name),
                godkjent = JaNeiVetIkkeResponse.valueOf(utdanningResultRow[UtdanningTable.godkjent].name)
            )
        } ?: throw RuntimeException("Fant ikke utdanning: $utdanningId")
    }

    private fun hentHelseResponse(helseId: Long): HelseResponse {
        return HelseTable.select { HelseTable.id eq helseId }
            .singleOrNull()?.let { helseResultRow ->
            HelseResponse(
                helseTilstandHindrerArbeid = JaNeiVetIkkeResponse.valueOf(helseResultRow[HelseTable.helsetilstandHindrerArbeid].name)
            )
        } ?: throw RuntimeException("Fant ikke helse: $helseId")
    }

    private fun hentArbeidserfaringResponse(arbeidserfaringId: Long): ArbeidserfaringResponse {
        return ArbeidserfaringTable.select { ArbeidserfaringTable.id eq arbeidserfaringId }
            .singleOrNull()?.let { arbeidserfaringResultRow ->
            ArbeidserfaringResponse(
                harHattArbeid = JaNeiVetIkkeResponse.valueOf(arbeidserfaringResultRow[ArbeidserfaringTable.harHattArbeid].name)
            )
        } ?: throw RuntimeException("Fant ikke arbeidserfaring: $arbeidserfaringId")
    }

    private fun hentBeskrivelseMedDetaljerResponse(situasjonId: Long): List<BeskrivelseMedDetaljerResponse> {
        return BeskrivelseMedDetaljerTable.select { BeskrivelseMedDetaljerTable.situasjonId eq situasjonId }
            .map { beskrivelseMedDetaljer ->
                val beskrivelseMedDetaljerId = beskrivelseMedDetaljer[BeskrivelseMedDetaljerTable.id].value
                val beskrivelse = hentBeskrivelseResponse(beskrivelseMedDetaljerId)
                val detaljer = hentDetaljerResponse(beskrivelseMedDetaljerId)
                BeskrivelseMedDetaljerResponse(
                    beskrivelse = beskrivelse,
                    detaljer = detaljer
                )
            }
    }

    private fun hentBeskrivelseResponse(beskrivelseMedDetaljerId: Long): BeskrivelseResponse {
        return BeskrivelseTable.select { BeskrivelseTable.beskrivelseMedDetaljerId eq beskrivelseMedDetaljerId }
            .singleOrNull()?.let { beskrivelse ->
            BeskrivelseResponse.valueOf(beskrivelse[BeskrivelseTable.beskrivelse].name)
        } ?: throw RuntimeException("Fant ikke beskrivelse: $beskrivelseMedDetaljerId")
    }

    private fun hentDetaljerResponse(beskrivelseId: Long): Map<String, String> {
        return DetaljerTable.select { DetaljerTable.beskrivelseId eq beskrivelseId }
            .associate { detaljerResultRow ->
                detaljerResultRow[DetaljerTable.noekkel] to detaljerResultRow[DetaljerTable.verdi]
            }
    }
}
