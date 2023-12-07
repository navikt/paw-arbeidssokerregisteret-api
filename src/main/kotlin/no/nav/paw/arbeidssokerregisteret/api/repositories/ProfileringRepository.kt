package no.nav.paw.arbeidssokerregisteret.api.repositories

import no.nav.paw.arbeidssokerregisteret.api.database.OpplysningerOmArbeidssoekerTable
import no.nav.paw.arbeidssokerregisteret.api.database.ProfileringTable
import no.nav.paw.arbeidssokerregisteret.api.domain.response.OpplysningerOmArbeidssoekerResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.ProfileringResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.toMetadataResponse
import no.nav.paw.arbeidssokerregisteret.api.domain.response.toProfilertTilResponse
import no.nav.paw.arbeidssokerregisteret.api.v1.Profilering
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ProfileringRepository(private val database: Database) {
    fun hentProfileringForArbeidssoekerMedPeriodeId(periodeId: UUID): List<ProfileringResponse> {
        return transaction(database) {
            ProfileringTable.select {
                ProfileringTable.periodeId eq periodeId
            }.map { resultRow ->
                ProfileringConverter(this@ProfileringRepository).konverterTilProfileringResponse(resultRow)
            }
        }
    }

    fun hentProfileringForArbeidssoekerMedOpplysningerOmArbeidssoekerId(opplysningerOmArbeidssoekerId: UUID): List<ProfileringResponse> {
        return transaction(database) {
            ProfileringTable.select {
                ProfileringTable.opplysningerOmArbeidssoekerId eq opplysningerOmArbeidssoekerId
            }.map { resultRow ->
                ProfileringConverter(this@ProfileringRepository).konverterTilProfileringResponse(resultRow)
            }
        }
    }

    fun opprettProfileringForArbeidssoeker(profilering: Profilering) {
        transaction(database) {
            val sendtInnAvId = PeriodeRepository(database).settInnMetadata(profilering.sendtInnAv)
            ProfileringTable.insert {
                it[profileringId] = profilering.id
                it[periodeId] = profilering.periodeId
                it[opplysningerOmArbeidssoekerId] = profilering.opplysningerOmArbeidssokerId
                it[ProfileringTable.sendtInnAvId] = sendtInnAvId
                it[profilertTil] = profilering.profilertTil
                it[jobbetSammenhengendeSeksAvTolvSisteManeder] = profilering.jobbetSammenhengendeSeksAvTolvSisteMnd
                it[alder] = profilering.alder
            }
        }
    }

    fun hentOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoekerId: UUID): OpplysningerOmArbeidssoekerResponse? =
        transaction(database) {
            OpplysningerOmArbeidssoekerTable.select {
                OpplysningerOmArbeidssoekerTable.opplysningerOmArbeidssoekerId eq opplysningerOmArbeidssoekerId
            }.singleOrNull()?.let { OpplysningerOmArbeidssoekerConverter().konverterTilOpplysningerOmArbeidssoekerResponse(it) }
        }

    fun hentMetadata(metadataId: Long) = PeriodeRepository(database).hentMetadata(metadataId)
}

class ProfileringConverter(private val profileringRepository: ProfileringRepository) {
    fun konverterTilProfileringResponse(resultRow: ResultRow): ProfileringResponse {
        val profileringId = resultRow[ProfileringTable.profileringId]
        val periodeId = resultRow[ProfileringTable.periodeId]
        val opplysningerOmArbeidssoekerId = resultRow[ProfileringTable.opplysningerOmArbeidssoekerId]
        val sendtInnAvId = resultRow[ProfileringTable.sendtInnAvId]
        val profilertTil = resultRow[ProfileringTable.profilertTil]
        val jobbetSammenhengendeSeksAvTolvSisteManeder = resultRow[ProfileringTable.jobbetSammenhengendeSeksAvTolvSisteManeder]
        val alder = resultRow[ProfileringTable.alder]

        val opplysningerOmArbeidssoeker = profileringRepository.hentOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoekerId) ?: throw Error("Fant ikke opplysninger om arbeidssøker")
        val sendtInnAv = profileringRepository.hentMetadata(sendtInnAvId)?.toMetadataResponse() ?: throw Error("Fant ikke metadata")

        return ProfileringResponse(
            profileringId,
            periodeId,
            opplysningerOmArbeidssoeker,
            sendtInnAv,
            profilertTil.toProfilertTilResponse(),
            jobbetSammenhengendeSeksAvTolvSisteManeder,
            alder
        )
    }
}
