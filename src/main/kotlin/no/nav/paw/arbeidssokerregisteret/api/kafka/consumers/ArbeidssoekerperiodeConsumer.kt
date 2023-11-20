package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssoekerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class ArbeidssoekerperiodeConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, Periode>,
    private val arbeidssoekerperiodeService: ArbeidssoekerperiodeService
) {
    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    logger.trace("Mottok melding fra $topic med offset ${post.offset()} partition ${post.partition()}")
                    val arbeidssoekerperiode = post.value()
                    arbeidssoekerperiodeService.opprettEllerOppdaterArbeidssoekerperiode(arbeidssoekerperiode)

                    consumer.commitSync()
                } catch (error: Exception) {
                    throw Exception("Feil ved konsumering av melding fra $topic", error)
                }
            }
        }
    }
}
