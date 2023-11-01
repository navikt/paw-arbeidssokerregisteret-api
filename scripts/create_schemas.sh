SUBJECT=paw-arbeidssokerperioder-v1-value
SCHEMA=`cat arbeidssokerperioder-v1.json | jq -c .`

curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d $SCHEMA \
    http://localhost:8082/subjects/$SUBJECT/versions
