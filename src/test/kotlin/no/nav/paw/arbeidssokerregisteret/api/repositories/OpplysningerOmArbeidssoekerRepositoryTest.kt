package no.nav.paw.arbeidssokerregisteret.api.repositories

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.paw.arbeidssokerregisteret.api.v1.Annet
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.BeskrivelseMedDetaljer
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Helse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
import no.nav.paw.arbeidssokerregisteret.api.v1.Jobbsituasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.OpplysningerOmArbeidssoeker
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanning
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanningsnivaa
import org.jetbrains.exposed.sql.Database
import java.time.Instant
import java.util.*
import javax.sql.DataSource

class OpplysningerOmArbeidssoekerRepositoryTest : StringSpec({

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

    "Opprett og hent ut en opplysninger om arbeidssøker" {
        val repository = OpplysningerOmArbeidssoekerRepository(database)
        val opplysninger = lagTestOpplysningerOmArbeidssoeker(periodeId1)
        repository.opprettOpplysningerOmArbeidssoeker(opplysninger)

        val retrievedOpplysninger = repository.hentOpplysningerOmArbeidssoeker(opplysninger.periodeId)

        retrievedOpplysninger.size shouldBe 1
    }

    "Opprett og hent ut flere opplysninger om arbeidssøker" {
        val repository = OpplysningerOmArbeidssoekerRepository(database)
        val opplysninger1 = lagTestOpplysningerOmArbeidssoeker(periodeId2)
        val opplysninger2 = lagTestOpplysningerOmArbeidssoeker(periodeId2)
        repository.opprettOpplysningerOmArbeidssoeker(opplysninger1)
        repository.opprettOpplysningerOmArbeidssoeker(opplysninger2)

        val retrievedOpplysninger = repository.hentOpplysningerOmArbeidssoeker(periodeId2)

        retrievedOpplysninger.size shouldBe 2
    }

    "Hent ut ikke-eksisterende opplysninger om arbeidssøker" {
        val repository = OpplysningerOmArbeidssoekerRepository(database)

        val retrievedOpplysninger = repository.hentOpplysningerOmArbeidssoeker(UUID.randomUUID())

        retrievedOpplysninger.size shouldBe 0
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

fun lagTestOpplysningerOmArbeidssoeker(periodeId: UUID) =
    OpplysningerOmArbeidssoeker(
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
        Jobbsituasjon(
            listOf(
                BeskrivelseMedDetaljer(
                    Beskrivelse.AKKURAT_FULLFORT_UTDANNING,
                    hentMapAvDetaljer()
                ),
                BeskrivelseMedDetaljer(
                    Beskrivelse.IKKE_VAERT_I_JOBB_SISTE_2_AAR,
                    hentMapAvDetaljer()
                )
            )
        ),
        Annet(
            JaNeiVetIkke.VET_IKKE
        )
    )

fun hentMapAvDetaljer(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    map["noekkel1"] = "verdi1"
    map["noekkel2"] = "verdi2"
    return map
}
