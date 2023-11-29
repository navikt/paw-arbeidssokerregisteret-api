package no.nav.paw.arbeidssokerregisteret.api.repositories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidsoekersituasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Element
import no.nav.paw.arbeidssokerregisteret.api.v1.Helse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanning
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanningsnivaa
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class SituasjonRepositoryTest : StringSpec({

    lateinit var dataSource: DataSource
    lateinit var database: Database
    val periodeId1: UUID = UUID.fromString("84201f96-363b-4aab-a589-89fa4b9b1feb")
    val periodeId2: UUID = UUID.fromString("84201f96-363b-4aab-a589-89fa4b9b1fec")

    beforeSpec {
        dataSource = initTestDatabase()
        database = Database.connect(dataSource)
        settInnTestPeriode(database, periodeId1)
        settInnTestPeriode(database, periodeId2)
    }

    afterSpec {
        dataSource.connection.close()
    }

    "Opprett og hent ut en situasjon" {
        val repository = SituasjonRepository(database)
        val situasjonBesvarelse = lagTestSituasjonBesvarelse(periodeId1)
        repository.opprettSituasjon(situasjonBesvarelse)

        val retrievedSituasjonBesvarelser = repository.hentSituasjoner(situasjonBesvarelse.periodeId)

        retrievedSituasjonBesvarelser.size shouldBe 1
    }

    "Opprett og hent ut flere situasjoner" {
        val repository = SituasjonRepository(database)
        val situasjonBesvarelse1 = lagTestSituasjonBesvarelse(periodeId2)
        val situasjonBesvarelse2 = lagTestSituasjonBesvarelse(periodeId2)
        repository.opprettSituasjon(situasjonBesvarelse1)
        repository.opprettSituasjon(situasjonBesvarelse2)

        val retrievedSituasjonBesvarelser = repository.hentSituasjoner(periodeId2)

        retrievedSituasjonBesvarelser.size shouldBe 2
    }

    "Hent ut en ikke-eksisterende situasjon" {
        val repository = SituasjonRepository(database)

        val retrievedSituasjonBesvarelse = repository.hentSituasjoner(UUID.randomUUID())

        retrievedSituasjonBesvarelse.size shouldBe 0
    }
})

fun settInnTestPeriode(
    database: Database,
    periodeId: UUID
) {
    val periodeRepository = PeriodeRepository(database)
    val periode = hentTestPeriode(periodeId)
    periodeRepository.opprettPeriode(periode)
}

fun lagTestSituasjonBesvarelse(periodeId: UUID) =
    Situasjon(
        UUID.randomUUID(),
        periodeId,
        Metadata(
            Instant.now(),
            Bruker(
                BrukerType.SYSTEM,
                "12345678911"
            ),
            "test",
            "test"
        ),
        Utdanning(
            Utdanningsnivaa.GRUNNSKOLE,
            JaNeiVetIkke.VET_IKKE,
            JaNeiVetIkke.VET_IKKE
        ),
        Helse(
            JaNeiVetIkke.VET_IKKE
        ),
        Arbeidserfaring(
            JaNeiVetIkke.VET_IKKE
        ),
        Arbeidsoekersituasjon(
            listOf(
                Element(
                    Beskrivelse.AKKURAT_FULLFORT_UTDANNING,
                    hentMapAvDetaljer()
                ),
                Element(
                    Beskrivelse.IKKE_VAERT_I_JOBB_SISTE_2_AAR,
                    hentMapAvDetaljer()
                )
            )
        )
    )

fun hentMapAvDetaljer(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    map["noekkel1"] = "verdi1"
    map["noekkel2"] = "verdi2"
    return map
}
