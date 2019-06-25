#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}

TAG=$1
[[ -z "$TAG" ]] && { echo "Error: image TAG not provided"; exit 1; }

./mvnw clean install -DskipITs && docker build --no-cache -t troyhart/yardsale-app:$TAG app/.
