package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidserfaringTable
import no.nav.paw.arbeidssokerregisteret.api.database.ArbeidsokersituasjonTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelseMedDetaljerTable
import no.nav.paw.arbeidssokerregisteret.api.database.BeskrivelseTable
import no.nav.paw.arbeidssokerregisteret.api.database.BrukerTable
import no.nav.paw.arbeidssokerregisteret.api.database.DetaljTable
import no.nav.paw.arbeidssokerregisteret.api.database.HelseTable
import no.nav.paw.arbeidssokerregisteret.api.database.MetadataTable
import no.nav.paw.arbeidssokerregisteret.api.database.SituasjonTable
import no.nav.paw.arbeidssokerregisteret.api.database.UtdanningTable
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidserfaringResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ArbeidssokersituasjonResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseMedDetaljerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BeskrivelseResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.BrukerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.DetaljResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.JaNeiVetIkkeResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.MetadataResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.UtdanningsnivaaResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.toBrukerTypeResponse
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
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
    fun konverterTilArbeidssokersituasjonResponse(resultRow: ResultRow): ArbeidssokersituasjonResponse {
        val periodeId = resultRow[SituasjonTable.periodeId]
        val sendtInnAvMetadata = MetadataTable.select { MetadataTable.id eq resultRow[SituasjonTable.sendtInnAv] }.singleOrNull()
            ?: throw Error("Fant ikke metadata")
        val utdanning = UtdanningTable.select { UtdanningTable.id eq resultRow[SituasjonTable.utdanningId] }.singleOrNull()
            ?: throw Error("Fant ikke utdanning")
        val helse = HelseTable.select { HelseTable.id eq resultRow[SituasjonTable.helseId] }.singleOrNull()
            ?: throw Error("Fant ikke helse")
        val arbeidserfaring = ArbeidserfaringTable.select { ArbeidserfaringTable.id eq resultRow[SituasjonTable.arbeidserfaringId] }.singleOrNull()
            ?: throw Error("Fant ikke arbeidserfaring")
        val arbeidssokersituasjon = ArbeidsokersituasjonTable.select { ArbeidsokersituasjonTable.id eq resultRow[SituasjonTable.id] }.singleOrNull()
            ?: throw Error("Fant ikke arbeidssokersituasjon")
        val beskrivelseMedDetaljer =
            BeskrivelseMedDetaljerTable.select { BeskrivelseMedDetaljerTable.arbeidssokersituasjonId eq arbeidssokersituasjon[ArbeidsokersituasjonTable.id] }.singleOrNull()
                ?: throw Error("Fant ikke beskrivelseMedDetaljer")
        val beskrivelser =
            BeskrivelseTable.select { BeskrivelseTable.id eq beskrivelseMedDetaljer[BeskrivelseMedDetaljerTable.id] }
        val detaljer = DetaljTable.select { DetaljTable.beskrivelseId eq beskrivelseMedDetaljer[BeskrivelseMedDetaljerTable.id] }

        return ArbeidssokersituasjonResponse(
            periodeId = periodeId,
            sendtInnAv = MetadataResponse(
                tidspunkt = sendtInnAvMetadata[MetadataTable.tidspunkt],
                utfoertAv = BrukerResponse(sendtInnAvMetadata[BrukerTable.type].toBrukerTypeResponse()),
                kilde = sendtInnAvMetadata[MetadataTable.kilde],
                aarsak = sendtInnAvMetadata[MetadataTable.aarsak]
            ),
            utdanning = UtdanningResponse(
                utdanningsnivaa = UtdanningsnivaaResponse.valueOf(utdanning[UtdanningTable.lengde].name),
                bestatt = JaNeiVetIkkeResponse.valueOf(utdanning[UtdanningTable.bestaatt].name),
                godkjent = JaNeiVetIkkeResponse.valueOf(utdanning[UtdanningTable.godkjent].name)
            ),
            helse = JaNeiVetIkkeResponse.valueOf(helse[HelseTable.helsetilstandHindrerArbeid].name),
            arbeidserfaring = ArbeidserfaringResponse(
                harHattArbeid = JaNeiVetIkkeResponse.valueOf(arbeidserfaring[ArbeidserfaringTable.harHattArbeid].name)
            ),
            arbeidssokersituasjon = beskrivelser.map { beskrivelse ->
                BeskrivelseMedDetaljerResponse(
                    beskrivelse = BeskrivelseResponse.valueOf(beskrivelse[BeskrivelseTable.beskrivelse].name),
                    detaljer = detaljer.map { detalj ->
                        DetaljResponse(detalj[DetaljTable.nokkel], detalj[DetaljTable.verdi])
                    }
                )
            }
        )
    }
}

class SituasjonBesvarelseRepository(private val database: Database) {
    fun hentSituasjonBesvarelse(periodeId: UUID): ArbeidssokersituasjonResponse? =
        transaction(database) {
            SituasjonTable.select { SituasjonTable.periodeId eq periodeId }.singleOrNull()?.let { resultRow ->
                SituasjonBesvarelseConverter().konverterTilArbeidssokersituasjonResponse(resultRow)
            }
        }

    fun hentSituasjonBesvarelser(periodeId: UUID): List<ArbeidssokersituasjonResponse> =
        transaction(database) {
            SituasjonTable.select {
                SituasjonTable.periodeId eq periodeId
            }.map { resultRow ->
                SituasjonBesvarelseConverter().konverterTilArbeidssokersituasjonResponse(resultRow)
            }
        }

    fun opprettSituasjonsBesvarelse(arbeidssokersituasjon: Situasjon) {
        transaction(database) {
            try {
                val sendtInnAvId = insertMetadata(arbeidssokersituasjon.sendtInnAv)
                val utdanningId = insertUtdanning(arbeidssokersituasjon.utdanning)
                val helseId = insertHelse(arbeidssokersituasjon.helse)
                val arbeidserfaringId = insertArbeidserfaring(arbeidssokersituasjon.arbeidserfaring)
                val situasjonId = insertSituasjon(arbeidssokersituasjon, sendtInnAvId, utdanningId, helseId, arbeidserfaringId)
                val arbeidssokersituasjonId = insertArbeidsokersituasjon(situasjonId)
                val beskrivelseMedDetaljerId = insertBeskrivelseMedDetaljer(arbeidssokersituasjonId)

                arbeidssokersituasjon.arbeidsoekersituasjon.beskrivelser.forEach { beskrivelseMedDetaljer ->
                    val beskrivelseId = insertBeskrivelse(beskrivelseMedDetaljer.beskrivelse, beskrivelseMedDetaljerId)
                    beskrivelseMedDetaljer.detaljer.forEach { detalj ->
                        insertDetalj(beskrivelseId, detalj)
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
            it[type] = bruker.type
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
        arbeidssokersituasjon: Situasjon,
        sendtInnAvId: Long,
        utdanningId: Long,
        helseId: Long,
        arbeidserfaringId: Long
    ): Long =
        SituasjonTable.insertAndGetId {
            it[periodeId] = arbeidssokersituasjon.periodeId
            it[sendtInnAv] = sendtInnAvId
            it[SituasjonTable.utdanningId] = utdanningId
            it[SituasjonTable.helseId] = helseId
            it[SituasjonTable.arbeidserfaringId] = arbeidserfaringId
        }.value

    private fun insertArbeidsokersituasjon(situasjonId: Long): Long =
        ArbeidsokersituasjonTable.insertAndGetId {
            it[id] = situasjonId
        }.value

    private fun insertBeskrivelseMedDetaljer(arbeidssokersituasjonId: Long): Long =
        BeskrivelseMedDetaljerTable.insertAndGetId {
            it[BeskrivelseMedDetaljerTable.arbeidssokersituasjonId] = arbeidssokersituasjonId
        }.value

    private fun insertBeskrivelse(beskrivelse: Beskrivelse, beskrivelseMedDetaljerId: Long): Long =
        BeskrivelseTable.insertAndGetId {
            it[BeskrivelseTable.beskrivelse] = Beskrivelse.valueOf(beskrivelse.name)
            it[BeskrivelseTable.beskrivelseMedDetaljerId] = beskrivelseMedDetaljerId
        }.value

    private fun insertDetalj(beskrivelseId: Long, detalj: Map.Entry<String, String>) {
        DetaljTable.insert {
            it[DetaljTable.beskrivelseId] = beskrivelseId
            it[nokkel] = detalj.key
            it[verdi] = detalj.value
        }
    }
}
