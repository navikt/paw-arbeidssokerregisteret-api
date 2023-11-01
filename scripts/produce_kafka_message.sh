DOCKER_CONTAINER_NAME=`docker ps --format '{{.Names}}' | grep schema`
KAFKA_MELDING=`cat ../src/main/resources/arbeidssokerperioder-kafka-melding.json | jq -c .`

echo $KAFKA_MELDING | docker exec -i $DOCKER_CONTAINER_NAME /usr/bin/kafka-avro-console-producer \
  --broker-list kafka:29092 \
  --topic paw-arbeidssokerperioder-v1 \
  --property schema.registry.url=http://localhost:8082 \
  --property value.schema.id=1
