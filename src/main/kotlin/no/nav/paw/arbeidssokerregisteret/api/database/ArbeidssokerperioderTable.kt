package no.nav.paw.arbeidssokerregisteret.api.database

import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object BrukerTable : LongIdTable("bruker") {
    val brukerId = varchar("brukerId", 255)
    val type = enumerationByName("type", 255, BrukerType::class)
}

object MetadataTable : LongIdTable("metadata") {
    val utfoertAvId = long("utfoertAvId").references(BrukerTable.id)
    val tidspunkt = timestamp("tidspunkt")
    val kilde = varchar("kilde", 255)
    val aarsak = varchar("aarsak", 255)
}

object ArbeidssokerperioderTable : LongIdTable("arbeidssokerperioder") {
    val periodeId = uuid("periodeId")
    val identitetsnummer = varchar("identitetsnummer", 11)
    val startetId = long("startetId").references(MetadataTable.id)
    val avsluttetId = long("avsluttetId").references(MetadataTable.id).nullable()
}


