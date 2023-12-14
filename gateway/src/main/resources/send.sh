#!/usr/bin/env bash

id=$(curl -X POST -H "Content-Type: application/json" --data @request.json localhost:8083/api/shopping | grep -oP '(?<="id":").*?(?=")')
sleep 10
curl -H "Content-Type: application/json" "localhost:8083/api/shopping/$id"
echo