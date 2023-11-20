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

class SituasjonBesvarelserRepositoryTest : StringSpec({

    lateinit var dataSource: DataSource
    lateinit var database: Database

    beforeSpec {
        dataSource = initTestDatabase()
        database = Database.connect(dataSource)
    }

    afterSpec {
        dataSource.connection.close()
    }

    "Insert and retrieve situasjonbesvarelser" {
        val periodeRepository = ArbeidssoekerperiodeRepository(database)
        val periode = createTestPeriode(UUID.fromString("84201f96-363b-4aab-a589-89fa4b9b1feb"))
        periodeRepository.opprettArbeidssoekerperiode(periode)

        val repository = SituasjonBesvarelserRepository(database)
        val situasjonBesvarelse = createTestSituasjonBesvarelse()
        repository.opprettSituasjonBesvarelse(situasjonBesvarelse)

        val retrievedSituasjonBesvarelser = repository.hentSituasjonBesvarelser(situasjonBesvarelse.periodeId)

        retrievedSituasjonBesvarelser.size shouldBe 1
    }
})

fun createTestSituasjonBesvarelse() =
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
                    getListOfDetaljerAsMap()
                ),
                Element(
                    Beskrivelse.IKKE_VAERT_I_JOBB_SISTE_2_AAR,
                    getListOfDetaljerAsMap()
                )
            )
        )

    )

fun getListOfDetaljerAsMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    map["nokkel1"] = "verdi1"
    map["nokkel2"] = "verdi2"
    return map
}
