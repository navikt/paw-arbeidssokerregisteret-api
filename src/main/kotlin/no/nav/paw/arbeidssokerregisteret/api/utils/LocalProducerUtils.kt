package no.nav.paw.arbeidssokerregisteret.api.utils

import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidserfaring
import no.nav.paw.arbeidssokerregisteret.api.v1.Arbeidsoekersituasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Beskrivelse
import no.nav.paw.arbeidssokerregisteret.api.v1.Bruker
import no.nav.paw.arbeidssokerregisteret.api.v1.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.v1.Element
import no.nav.paw.arbeidssokerregisteret.api.v1.Helse
import no.nav.paw.arbeidssokerregisteret.api.v1.JaNeiVetIkke
import no.nav.paw.arbeidssokerregisteret.api.v1.Metadata
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanning
import no.nav.paw.arbeidssokerregisteret.api.v1.Utdanningsnivaa
import java.time.Instant
import java.util.UUID

class LocalProducerUtils {
    val testUUID1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val testUUID2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

    fun lagTestPerioder(): List<Periode> {
        return listOf(
            Periode(
                testUUID1,
                "12345678911",
                Metadata(
                    Instant.now(),
                    Bruker(
                        BrukerType.UKJENT_VERDI,
                        "12345678911"
                    ),
                    "test",
                    "test"
                ),
                null
            ),
            Periode(
                testUUID2,
                "12345678911",
                Metadata(
                    Instant.now(),
                    Bruker(
                        BrukerType.UKJENT_VERDI,
                        "12345678911"
                    ),
                    "test",
                    "test"
                ),
                Metadata(
                    Instant.now().plusSeconds(100),
                    Bruker(
                        BrukerType.UKJENT_VERDI,
                        "12345678911"
                    ),
                    "test",
                    "test"
                )
            )
        )
    }

    fun lagTestSituasjoner(): List<Situasjon> {
        return listOf(
            Situasjon(
                UUID.randomUUID(),
                testUUID1,
                Metadata(
                    Instant.now(),
                    Bruker(
                        BrukerType.UKJENT_VERDI,
                        "12345678911"
                    ),
                    "test",
                    "test"
                ),
                Utdanning(
                    Utdanningsnivaa.GRUNNSKOLE,
                    JaNeiVetIkke.JA,
                    JaNeiVetIkke.JA
                ),
                Helse(
                    JaNeiVetIkke.JA
                ),
                Arbeidserfaring(
                    JaNeiVetIkke.JA
                ),
                Arbeidsoekersituasjon(
                    listOf(
                        Element(
                            Beskrivelse.AKKURAT_FULLFORT_UTDANNING,
                            mapOf(
                                Pair("test", "test"),
                                Pair("test", "test")
                            )
                        ),
                        Element(
                            Beskrivelse.DELTIDSJOBB_VIL_MER,
                            mapOf(
                                Pair("test", "test"),
                                Pair("test", "test")
                            )
                        )
                    )
                )
            )
        )
    }
}
