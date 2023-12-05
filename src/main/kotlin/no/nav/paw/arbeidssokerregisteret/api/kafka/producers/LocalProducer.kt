package no.nav.paw.arbeidssokerregisteret.api.kafka.producers

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.paw.arbeidssokerregisteret.api.config.KafkaConfig
import no.nav.paw.arbeidssokerregisteret.api.config.properties
import no.nav.paw.arbeidssokerregisteret.api.v1.Periode
import no.nav.paw.arbeidssokerregisteret.api.v1.Situasjon
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord

class LocalProducer(private val kafkaConfig: KafkaConfig) {
    private val periodeProducer: Producer<String, Periode> = createProducer()
    private val situasjonProducer: Producer<String, Situasjon> = createProducer()

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

    fun produceSituasjonMessage(
        topic: String,
        key: String,
        value: Situasjon
    ) {
        val record = ProducerRecord(topic, key, value)
        situasjonProducer.send(record) { _, exception ->
            if (exception != null) {
                println("Failed to send message: $exception")
            } else {
                println("Message sent successfully to topic: $topic")
            }
        }
    }

    fun close() {
        periodeProducer.close()
        situasjonProducer.close()
    }
}
