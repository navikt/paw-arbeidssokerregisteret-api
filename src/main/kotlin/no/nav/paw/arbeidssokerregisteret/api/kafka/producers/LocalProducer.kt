package no.nav.paw.arbeidssokerregisteret.api.kafka.producers

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.paw.arbeidssokerregisteret.api.config.Config
import no.nav.paw.arbeidssokerregisteret.api.config.KafkaConfig
import no.nav.paw.arbeidssokerregisteret.api.config.properties
import no.nav.paw.arbeidssokerregisteret.api.utils.LocalProducerUtils
import no.nav.paw.arbeidssokerregisteret.api.utils.loadConfiguration
import no.nav.paw.arbeidssokerregisteret.api.v1.OpplysningerOmArbeidssoeker
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord

fun main() {
    val config = loadConfiguration<Config>()
    produserMeldingerForLokalUtvikling(config.kafka)
}

fun produserMeldingerForLokalUtvikling(kafkaConfig: KafkaConfig) {
    val localProducer = LocalProducer(kafkaConfig)
    try {
        LocalProducerUtils().lagTestPerioder().forEach {
            localProducer.producePeriodeMessage(kafkaConfig.periodeTopic, it.id.toString(), it)
        }

        LocalProducerUtils().lagTestOpplysningerOmArbeidssoeker().forEach {
            localProducer.produceOpplysningerOmArbeidssoekerMessage(kafkaConfig.opplysningerOmArbeidssoekerTopic, it.id.toString(), it)
        }
    } catch (e: Exception) {
        println("LocalProducer error: ${e.message}")
        localProducer.close()
    }
}

class LocalProducer(private val kafkaConfig: KafkaConfig) {
    private val periodeProducer: Producer<String, Periode> = createProducer()
    private val opplysningerOmArbeidssoekerProducer: Producer<String, OpplysningerOmArbeidssoeker> = createProducer()

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

    fun close() {
        periodeProducer.close()
        opplysningerOmArbeidssoekerProducer.close()
    }
}
