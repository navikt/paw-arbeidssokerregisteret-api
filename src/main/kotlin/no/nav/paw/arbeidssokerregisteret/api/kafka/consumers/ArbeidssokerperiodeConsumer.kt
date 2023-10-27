package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.paw.arbeidssokerregisteret.api.domain.ArbeidssokerperiodeKafkaMelding
import no.nav.paw.arbeidssokerregisteret.api.services.ArbeidssokerperiodeService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class ArbeidssokerperiodeConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, String>,
    private val arbeidssokerperiodeService: ArbeidssokerperiodeService,
    private val objectMapper: ObjectMapper
) {

    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    val arbeidssokerperiode: ArbeidssokerperiodeKafkaMelding = objectMapper.readValue<ArbeidssokerperiodeKafkaMelding>(post.value())

                    logger.info("Mottok melding fra $topic med offset ${post.offset()}p${post.partition()}")

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
