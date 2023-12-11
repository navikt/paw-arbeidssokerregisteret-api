package no.nav.paw.arbeidssokerregisteret.api.kafka.consumers

import no.nav.paw.arbeidssokerregisteret.api.services.OpplysningerOmArbeidssoekerService
import no.nav.paw.arbeidssokerregisteret.api.utils.logger
import no.nav.paw.arbeidssokerregisteret.api.v1.OpplysningerOmArbeidssoeker
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

class OpplysningerOmArbeidssoekerConsumer(
    private val topic: String,
    private val consumer: KafkaConsumer<String, OpplysningerOmArbeidssoeker>,
    private val opplysningerOmArbeidssoekerService: OpplysningerOmArbeidssoekerService
) {
    fun start() {
        logger.info("Lytter på topic $topic")
        consumer.subscribe(listOf(topic))

        while (true) {
            consumer.poll(Duration.ofMillis(500)).forEach { post ->
                try {
                    logger.info("Mottok melding fra $topic med offset ${post.offset()} partition ${post.partition()}")
                    val opplysningerOmArbeidssoeker = post.value()
                    opplysningerOmArbeidssoekerService.opprettOpplysningerOmArbeidssoeker(opplysningerOmArbeidssoeker)

                    consumer.commitSync()
                } catch (error: Exception) {
                    throw Exception("Feil ved konsumering av melding fra $topic", error)
                }
            }
        }
    }
}
