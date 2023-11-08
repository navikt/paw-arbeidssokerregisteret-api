package no.nav.paw.arbeidssokerregisteret.api.domain

@JvmInline
value class Identitetsnummer(val verdi: String) {
    override fun toString(): String {
        return "*".repeat(11)
    }
}

fun String.toIdentitetsnummer(): Identitetsnummer = Identitetsnummer(this)
