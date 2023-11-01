package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import no.nav.paw.arbeidssokerregisteret.api.domain.mapper.tilArbeidssokerperiodeDto
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssokerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class ArbeidssokerperiodeConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, Periode>,
    private val arbeidssokerperiodeService: ArbeidssokerperiodeService
) {

    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    logger.info("Mottok melding fra $topic med offset ${post.offset()}p${post.partition()}")
                    val arbeidssokerperiode = post.value().tilArbeidssokerperiodeDto()

                    arbeidssokerperiodeService.opprettArbeidssokerperiode(arbeidssokerperiode)
                } catch (error: Exception) {
                    logger.error("Feil ved konsumering av melding fra $topic", error)
                    throw error
                }
                consumer.commitSync()
            }
        }
    }
}
