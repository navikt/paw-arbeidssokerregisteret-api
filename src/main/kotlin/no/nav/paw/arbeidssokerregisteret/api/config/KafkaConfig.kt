package no.nav.paw.arbeidssokerregisteret.api.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*

fun createKafkaConsumerConfig(kafkaConfig: KafkaConfig): KafkaConsumer<String, String> {
    // TODO: Oppsett for nais/aiven
    val consumerProperties = Properties().apply {
        this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaConfig.brokerUrl
        this[ConsumerConfig.GROUP_ID_CONFIG] = kafkaConfig.consumerGroupId
        this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
    }
    return KafkaConsumer<String, String>(consumerProperties)
}
