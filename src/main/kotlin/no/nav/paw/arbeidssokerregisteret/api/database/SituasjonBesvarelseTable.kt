package no.nav.paw.arbeidssokerregisteret.api.database

import no.nav.paw.arbeidssokerregisteret.api.utils.PGEnum
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanningsnivaa
import org.jetbrains.exposed.dao.id.LongIdTable

object SituasjonTable : LongIdTable("situasjon") {
    val periodeId = uuid("periode_id")
    val sendtInnAv = long("sendt_inn_av_id").references(MetadataTable.utfoertAvId)
    val utdanningId = long("utdanning_id").references(UtdanningTable.id)
    val helseId = long("helse_id").references(HelseTable.id)
    val arbeidserfaringId = long("arbeidserfaring_id").references(ArbeidserfaringTable.id)
}

object UtdanningTable : LongIdTable("utdanning") {
    val lengde = customEnumeration("lengde", "Utdanningsnivaa", { value -> Utdanningsnivaa.valueOf(value as String) }, { PGEnum("Utdanningsnivaa", it) })
    val bestaatt = customEnumeration("bestatt", "JaNeiVetIkke", { value -> JaNeiVetIkke.valueOf(value as String) }, { PGEnum("JaNeiVetIkke", it) })
    val godkjent = customEnumeration("godkjent", "JaNeiVetIkke", { value -> JaNeiVetIkke.valueOf(value as String) }, { PGEnum("JaNeiVetIkke", it) })
}

object HelseTable : LongIdTable("helse") {
    val helsetilstandHindrerArbeid = customEnumeration("helsetilstand_hindrer_arbeid", "JaNeiVetIkke", { value -> JaNeiVetIkke.valueOf(value as String) }, { PGEnum("JaNeiVetIkke", it) })
}

object ArbeidserfaringTable : LongIdTable("arbeidserfaring") {
    val situasjonId = reference("situasjon_id", SituasjonTable)
    val harHattArbeid = customEnumeration("har_hatt_arbeid", "JaNeiVetIkke", { value -> JaNeiVetIkke.valueOf(value as String) }, { PGEnum("JaNeiVetIkke", it) })
}

object ArbeidsokersituasjonTable : LongIdTable("arbeidssokersituasjon")

object BeskrivelseMedDetaljerTable : LongIdTable("beskrivelser") {
    val arbeidssokersituasjonId = reference("arbeidssokersituasjon_id", ArbeidsokersituasjonTable)
}

object BeskrivelseTable : LongIdTable("beskrivelse") {
    val beskrivelse = customEnumeration("beskrivelse", "Beskrivelse", { value -> Beskrivelse.valueOf(value as String) }, { PGEnum("Beskrivelse", it) })
    val beskrivelseMedDetaljerId = reference("beskrivelser_med_detaljer_id", BeskrivelseMedDetaljerTable)
}

object DetaljTable : LongIdTable("detalj") {
    val beskrivelseId = reference("beskrivelse_id", BeskrivelseTable)
    val nokkel = varchar("nokkel", 50)
    val verdi = varchar("verdi", 255)
}
