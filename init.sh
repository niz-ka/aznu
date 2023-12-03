#!/usr/bin/env bash

docker exec -it kafka bash -c "kafka-topics --create --if-not-exists --topic input-topic --bootstrap-server localhost:9092"
docker exec -it kafka bash -c "kafka-topics --create --if-not-exists --topic output-topic --bootstrap-server localhost:9092"
