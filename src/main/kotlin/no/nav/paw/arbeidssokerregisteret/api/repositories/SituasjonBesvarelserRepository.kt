package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidserfaringTable
import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidssoekersituasjonTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelseMedDetaljerTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelserTable
import no.nav.paw.arbeidssokerregisteret.api.database.BrukerTable
import no.nav.paw.arbeidssokerregisteret.api.database.DetaljerTable
import no.nav.paw.arbeidssokerregisteret.api.database.HelseTable
import no.nav.paw.arbeidssokerregisteret.api.database.MetadataTable
import no.nav.paw.arbeidssokerregisteret.api.database.SituasjonTable
import no.nav.paw.arbeidssokerregisteret.api.database.UtdanningTable
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidserfaringResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssoekersituasjonResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseMedDetaljerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BrukerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BrukerTypeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.JaNeiVetIkkeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.MetadataResponse
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

class SituasjonBesvarelseConverter {
    fun konverterTilArbeidssoekersituasjonResponse(resultRow: ResultRow): ArbeidssoekersituasjonResponse {
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
        val arbeidssoekersituasjon = ArbeidssoekersituasjonTable.select { ArbeidssoekersituasjonTable.situasjonId eq situasjonId.value }.singleOrNull()
            ?: throw Error("Fant ikke arbeidssoekersituasjon")
        val beskrivelseMedDetaljer = BeskrivelseMedDetaljerTable.select { BeskrivelseMedDetaljerTable.arbeidssoekersituasjonId eq arbeidssoekersituasjon[ArbeidssoekersituasjonTable.id].value }
            .map { beskrivelseMedDetaljerResultRow ->
                val beskrivelse = BeskrivelserTable.select { BeskrivelserTable.beskrivelseMedDetaljerId eq beskrivelseMedDetaljerResultRow[BeskrivelseMedDetaljerTable.id].value }.singleOrNull()
                    ?: throw Error("Fant ikke beskrivelse")
                val detaljer = DetaljerTable.select { DetaljerTable.beskrivelserId eq beskrivelse[BeskrivelserTable.id].value }.associate { detaljerResultRow ->
                    detaljerResultRow[DetaljerTable.nokkel] to detaljerResultRow[DetaljerTable.verdi]
                }
                BeskrivelseMedDetaljerResponse(
                    beskrivelse = BeskrivelseResponse.valueOf(beskrivelse[BeskrivelserTable.beskrivelse].name),
                    detaljer = detaljer
                )
            }

        return ArbeidssoekersituasjonResponse(
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

class SituasjonBesvarelserRepository(private val database: Database) {
    fun hentSituasjonBesvarelse(periodeId: UUID): ArbeidssoekersituasjonResponse? =
        transaction(database) {
            SituasjonTable.select { SituasjonTable.periodeId eq periodeId }.singleOrNull()?.let { resultRow ->
                SituasjonBesvarelseConverter().konverterTilArbeidssoekersituasjonResponse(resultRow)
            }
        }

    fun hentSituasjonBesvarelser(periodeId: UUID): List<ArbeidssoekersituasjonResponse> =
        transaction(database) {
            SituasjonTable.select {
                SituasjonTable.periodeId eq periodeId
            }.map { resultRow ->
                SituasjonBesvarelseConverter().konverterTilArbeidssoekersituasjonResponse(resultRow)
            }
        }

    fun opprettSituasjonBesvarelse(arbeidssoekersituasjon: Situasjon) {
        transaction(database) {
            try {
                val sendtInnAvId = insertMetadata(arbeidssoekersituasjon.sendtInnAv)
                val utdanningId = insertUtdanning(arbeidssoekersituasjon.utdanning)
                val helseId = insertHelse(arbeidssoekersituasjon.helse)
                val arbeidserfaringId = insertArbeidserfaring(arbeidssoekersituasjon.arbeidserfaring)
                val situasjonId = insertSituasjon(arbeidssoekersituasjon, sendtInnAvId, utdanningId, helseId, arbeidserfaringId)
                val arbeidssoekersituasjonId = insertArbeidssoekersituasjon(situasjonId)

                arbeidssoekersituasjon.arbeidsoekersituasjon.beskrivelser.forEach { beskrivelseMedDetaljer ->
                    val beskrivelseMedDetaljerId = insertBeskrivelseMedDetaljer(arbeidssoekersituasjonId)
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
        arbeidssoekersituasjon: Situasjon,
        sendtInnAvId: Long,
        utdanningId: Long,
        helseId: Long,
        arbeidserfaringId: Long
    ): Long =
        SituasjonTable.insertAndGetId {
            it[periodeId] = arbeidssoekersituasjon.periodeId
            it[sendtInnAv] = sendtInnAvId
            it[SituasjonTable.utdanningId] = utdanningId
            it[SituasjonTable.helseId] = helseId
            it[SituasjonTable.arbeidserfaringId] = arbeidserfaringId
        }.value

    private fun insertArbeidssoekersituasjon(situasjonId: Long): Long =
        ArbeidssoekersituasjonTable.insertAndGetId {
            it[ArbeidssoekersituasjonTable.situasjonId] = situasjonId
        }.value

    private fun insertBeskrivelseMedDetaljer(arbeidssoekersituasjonId: Long): Long =
        BeskrivelseMedDetaljerTable.insertAndGetId {
            it[BeskrivelseMedDetaljerTable.arbeidssoekersituasjonId] = arbeidssoekersituasjonId
        }.value

    private fun insertBeskrivelse(beskrivelse: Beskrivelse, beskrivelseMedDetaljerId: Long): Long =
        BeskrivelserTable.insertAndGetId {
            it[BeskrivelserTable.beskrivelse] = Beskrivelse.valueOf(beskrivelse.name)
            it[BeskrivelserTable.beskrivelseMedDetaljerId] = beskrivelseMedDetaljerId
        }.value

    private fun insertDetalj(beskrivelserId: Long, detalj: Map.Entry<String, String>) {
        DetaljerTable.insert {
            it[DetaljerTable.beskrivelserId] = beskrivelserId
            it[nokkel] = detalj.key
            it[verdi] = detalj.value
        }
    }
}
