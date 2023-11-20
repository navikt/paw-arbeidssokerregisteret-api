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

    beforeSpec {
        dataSource = initTestDatabase()
        database = Database.connect(dataSource)
        settInnTestPeriode(database)
    }

    afterSpec {
        dataSource.connection.close()
    }

    "Opprett og hent ut en situasjon" {
        val repository = SituasjonRepository(database)
        val situasjonBesvarelse = lagTestSituasjonBesvarelse()
        repository.opprettSituasjon(situasjonBesvarelse)

        val retrievedSituasjonBesvarelser = repository.hentSituasjoner(situasjonBesvarelse.periodeId)

        retrievedSituasjonBesvarelser.size shouldBe 1
    }

    "Opprett og hent ut flere situasjoner" {
        val repository = SituasjonRepository(database)
        val situasjonBesvarelse1 = lagTestSituasjonBesvarelse()
        val situasjonBesvarelse2 = lagTestSituasjonBesvarelse()
        repository.opprettSituasjon(situasjonBesvarelse1)
        repository.opprettSituasjon(situasjonBesvarelse2)

        val retrievedSituasjonBesvarelser = repository.hentSituasjoner(situasjonBesvarelse1.periodeId)

        retrievedSituasjonBesvarelser.size shouldBe 2
    }

    "Hent ut en ikke-eksisterende situasjon" {
        val repository = SituasjonRepository(database)

        val retrievedSituasjonBesvarelse = repository.hentSituasjon(UUID.randomUUID())

        retrievedSituasjonBesvarelse shouldBe null
    }
})

fun settInnTestPeriode(database: Database) {
    val periodeRepository = PeriodeRepository(database)
    val periode = hentTestPeriode(UUID.fromString("84201f96-363b-4aab-a589-89fa4b9b1feb"))
    periodeRepository.opprettPeriode(periode)
}

fun lagTestSituasjonBesvarelse() =
    Situasjon(
        UUID.randomUUID(),
        UUID.fromString("84201f96-363b-4aab-a589-89fa4b9b1feb"),
        Metadata(
            Instant.now(),
            Bruker(
                BrukerType.SYSTEM,
                "012345678911"
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
