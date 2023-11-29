package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import no.nav.paw.arbeidssokerregisteret.api.services.SituasjonService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class SituasjonConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, Situasjon>,
    private val situasjonService: SituasjonService
) {
    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    logger.trace("Mottok melding fra $topic med offset ${post.offset()} partition ${post.partition()}")
                    val arbeidssoekersituasjon = post.value()
                    situasjonService.opprettSituasjon(arbeidssoekersituasjon)

                    consumer.commitSync()
                } catch (error: Exception) {
                    throw Exception("Feil ved konsumering av melding fra $topic", error)
                }
            }
        }
    }
}
