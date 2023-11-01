package no.nav.paw.arbeidssokerregisteret.api.domain.mapper

import no.nav.paw.arbeidssokerregisteret.api.domain.Arbeidssokerperiode
import no.nav.paw.arbeidssokerregisteret.api.domain.Bruker
import no.nav.paw.arbeidssokerregisteret.api.domain.BrukerType
import no.nav.paw.arbeidssokerregisteret.api.utils.toLocalDateTime
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode

fun Periode.tilArbeidssokerperiodeDto(): Arbeidssokerperiode = Arbeidssokerperiode(
    id = this.id,
    identitetsnummer = this.identitetsnummer,
    startet = no.nav.paw.arbeidssokerregisteret.api.domain.Metadata(
        tidspunkt = this.startet.tidspunkt.toLocalDateTime(),
        utfoertAv = Bruker(
            type = BrukerType.valueOf(this.startet.utfoertAv.type.toString()),
            id = this.startet.utfoertAv.id
        ),
        kilde = this.startet.kilde,
        aarsak = this.startet.aarsak
    ),
    avsluttet = this.avsluttet?.let {
        no.nav.paw.arbeidssokerregisteret.api.domain.Metadata(
            tidspunkt = it.tidspunkt.toLocalDateTime(),
            utfoertAv = Bruker(
                type = BrukerType.valueOf(it.utfoertAv.type.toString()),
                id = it.utfoertAv.id
            ),
            kilde = it.kilde,
            aarsak = it.aarsak
        )
    }
)
