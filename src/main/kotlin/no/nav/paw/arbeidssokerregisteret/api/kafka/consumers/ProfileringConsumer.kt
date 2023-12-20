package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import no.nav.paw.arbeidssokerregisteret.api.services.ProfileringService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Profilering
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class ProfileringConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, Profilering>,
    private val profileringService: ProfileringService
) {
    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))
        consumer.seekToBeginning(consumer.assignment()) // TODO: fjernes etter testing i dev

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    logger.info("Mottok melding fra $topic med offset ${post.offset()} partition ${post.partition()}")
                    val profileringAvArbeidssoeker = post.value()
                    profileringService.opprettProfileringForArbeidssoeker(profileringAvArbeidssoeker)

                    consumer.commitSync()
                } catch (error: Exception) {
                    throw Exception("Feil ved konsumering av melding fra $topic", error)
                }
            }
        }
    }
}
