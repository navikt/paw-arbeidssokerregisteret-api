package no.nav.paw.arbeidssokerregisteret.api.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object ArbeidssokerperioderTable : IntIdTable("arbeidssokerperioder") {
    val foedselsnummer = varchar("foedselsnummer", 11).index()
    val startet = datetime("startet")
    val avsluttet = datetime("avsluttet").nullable()
}
