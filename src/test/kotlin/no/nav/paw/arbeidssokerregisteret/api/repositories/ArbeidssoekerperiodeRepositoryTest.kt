package no.nav.paw.arbeidssokerregisteret.api.repositories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.paw.arbeidssokerregisteret.api.domain.Identitetsnummer
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.util.UUID
import javax.sql.DataSource

class ArbeidssokerperiodeRepositoryTest : StringSpec({
    lateinit var dataSource: DataSource
    lateinit var database: Database

    beforeSpec {
        dataSource = initTestDatabase()
        database = Database.connect(dataSource)
    }

    afterSpec {
        dataSource.connection.close()
    }

    "Insert and retrieve a periode" {
        val repository = ArbeidssoekerperiodeRepository(database)

        val periode = createTestPeriode()
        repository.opprettArbeidssoekerperiode(periode)

        val retrievedPeriode = repository.hentArbeidssoekerperiodeMedPeriodeId(periode.id)

        retrievedPeriode shouldNotBe null
        retrievedPeriode!! shouldBe periode
    }

    "Retrieve arbeidssoekerperioder for a given Identitetsnummer" {
        val repository = ArbeidssoekerperiodeRepository(database)

        val identitetsnummer = Identitetsnummer("12345678911")

        val arbeidssoekerperioder = repository.hentArbeidssoekerperioder(identitetsnummer)

        arbeidssoekerperioder.size shouldBeExactly 1
    }

    "Update arbeidssoekerperiode with avsluttet metadata" {
        val repository = ArbeidssoekerperiodeRepository(database)

        val periode = createTestPeriode()
        repository.opprettArbeidssoekerperiode(periode)

        val updatedMetadata =
            Metadata(
                Instant.now(),
                Bruker(BrukerType.SYSTEM, "2"),
                "NY_KILDE",
                "NY_AARSAK"
            )

        val updatedPeriode = periode.copy(avsluttet = updatedMetadata)

        repository.oppdaterArbeidssoekerperiode(updatedPeriode)

        val retrievedPeriode = repository.hentArbeidssoekerperiodeMedPeriodeId(periode.id)

        retrievedPeriode shouldNotBe null
        retrievedPeriode!! shouldBe updatedPeriode
    }

    "Update arbeidssoekerperiode without avsluttet metadata" {
        val repository = ArbeidssoekerperiodeRepository(database)

        val periode = createTestPeriode().copy(avsluttet = null)
        repository.opprettArbeidssoekerperiode(periode)

        val updatedMetadata =
            Metadata(
                Instant.now(),
                Bruker(BrukerType.SYSTEM, "2"),
                "NY_KILDE",
                "NY_AARSAK"
            )
        val updatedPeriode = periode.copy(avsluttet = updatedMetadata)

        repository.oppdaterArbeidssoekerperiode(updatedPeriode)

        val retrievedPeriode = repository.hentArbeidssoekerperiodeMedPeriodeId(periode.id)

        retrievedPeriode shouldNotBe null
        retrievedPeriode shouldBe updatedPeriode
    }
    "Update arbeidssoekerperiode avsluttet metadata to null should not be possible" {
        val repository = ArbeidssoekerperiodeRepository(database)

        val periode = createTestPeriode()
        repository.opprettArbeidssoekerperiode(periode)

        val updatedPeriode = periode.copy(avsluttet = null)

        repository.oppdaterArbeidssoekerperiode(updatedPeriode)

        val retrievedPeriode = repository.hentArbeidssoekerperiodeMedPeriodeId(periode.id)

        retrievedPeriode shouldNotBe null
        retrievedPeriode shouldNotBe updatedPeriode
    }
})

fun createTestPeriode(periodeId: UUID? = null): Periode {
    val startetMetadata =
        Metadata(
            Instant.now(),
            Bruker(BrukerType.SLUTTBRUKER, "1"),
            "KILDE",
            "AARSAK"
        )
    val avsluttetMetadata =
        Metadata(
            Instant.now().plusMillis(100),
            Bruker(BrukerType.SYSTEM, "2"),
            "KILDE AVSLUTTET",
            "AARSAK AVSLUTTET"
        )
    return Periode(
        periodeId ?: UUID.randomUUID(),
        "12345678911",
        startetMetadata,
        avsluttetMetadata
    )
}

fun Periode.copy(avsluttet: Metadata?): Periode {
    return Periode(
        id,
        identitetsnummer,
        startet,
        avsluttet
    )
}
