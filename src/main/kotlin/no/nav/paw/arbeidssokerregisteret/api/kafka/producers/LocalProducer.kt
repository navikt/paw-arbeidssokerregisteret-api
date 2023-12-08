package no.nav.paw.arbeidssokerregisteret.api.kafka.producers

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.paw.arbeidssokerregisteret.api.config.KafkaConfig
import no.nav.paw.arbeidssokerregisteret.api.config.properties
import no.nav.paw.arbeidssokerregisteret.api.utils.LocalProducerUtils
import no.nav.paw.arbeidssokerregisteret.api.utils.loadLocalConfiguration
import no.nav.paw.arbeidssokerregisteret.api.v1.OpplysningerOmArbeidssoeker
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import no.nav.paw.arbeidssokerregisteret.api.v1.Profilering
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord

fun main() {
    val config = loadLocalConfiguration()
    produserPeriodeMeldinger(config.kafka)
    produserOpplysningerOmArbeidssoekerMeldinger(config.kafka)
    produserProfileringMeldinger(config.kafka)
}

fun produserPeriodeMeldinger(kafkaConfig: KafkaConfig) {
    val localProducer = LocalProducer(kafkaConfig)
    try {
        LocalProducerUtils().lagTestPerioder().forEach { periode ->
            localProducer.producePeriodeMessage(kafkaConfig.periodeTopic, periode.id.toString(), periode)
        }
    } catch (e: Exception) {
        println("LocalProducer periode error: ${e.message}")
        localProducer.close()
    }
}

fun produserOpplysningerOmArbeidssoekerMeldinger(kafkaConfig: KafkaConfig) {
    val localProducer = LocalProducer(kafkaConfig)
    try {
        LocalProducerUtils().lagTestOpplysningerOmArbeidssoeker().forEach { opplysninger ->
            localProducer.produceOpplysningerOmArbeidssoekerMessage(kafkaConfig.opplysningerOmArbeidssoekerTopic, opplysninger.id.toString(), opplysninger)
        }
    } catch (e: Exception) {
        println("LocalProducer opplysninger-om-arbeidssoeker error: ${e.message}")
        localProducer.close()
    }
}

fun produserProfileringMeldinger(kafkaConfig: KafkaConfig) {
    val localProducer = LocalProducer(kafkaConfig)
    try {
        LocalProducerUtils().lagTestProfilering().let { profilering ->
            localProducer.produceProfileringMessage(kafkaConfig.profileringTopic, profilering.id.toString(), profilering)
        }
    } catch (e: Exception) {
        println("LocalProducer profilering error: ${e.message}")
        localProducer.close()
    }
}

class LocalProducer(private val kafkaConfig: KafkaConfig) {
    private val periodeProducer: Producer<String, Periode> = createProducer()
    private val opplysningerOmArbeidssoekerProducer: Producer<String, OpplysningerOmArbeidssoeker> = createProducer()
    private val profileringProducer: Producer<String, Profilering> = createProducer()

    private fun <T> createProducer(): Producer<String, T> {
        val props = kafkaConfig.properties.toMutableMap()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringSerializer"
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.name
        props[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaConfig.schemaRegistryConfig.url

        return KafkaProducer(props)
    }

    fun producePeriodeMessage(
        topic: String,
        key: String,
        value: Periode
    ) {
        val record = ProducerRecord(topic, key, value)
        periodeProducer.send(record) { _, exception ->
            if (exception != null) {
                println("Failed to send message: $exception")
            } else {
                println("Message sent successfully to topic: $topic")
            }
        }
    }

    fun produceOpplysningerOmArbeidssoekerMessage(
        topic: String,
        key: String,
        value: OpplysningerOmArbeidssoeker
    ) {
        val record = ProducerRecord(topic, key, value)
        opplysningerOmArbeidssoekerProducer.send(record) { _, exception ->
            if (exception != null) {
                println("Failed to send message: $exception")
            } else {
                println("Message sent successfully to topic: $topic")
            }
        }
    }

    fun produceProfileringMessage(
        topic: String,
        key: String,
        value: Profilering
    ) {
        val record = ProducerRecord(topic, key, value)
        profileringProducer.send(record) { _, exception ->
            if (exception != null) {
                println("Failed to send message: $exception")
            } else {
                println("Message sent successfully to topic: $topic")
            }
        }
    }

    fun close() {
        periodeProducer.close()
        opplysningerOmArbeidssoekerProducer.close()
    }
}
