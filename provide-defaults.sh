#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR" || exit 1

ENV_FILE=.env
if [ ! -f "$ENV_FILE" ]; then
  echo \
"APP_URL_ROOTS=http://localhost:3000

MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=handwriting_labeling_app
MYSQL_USERNAME=xxxxxxxxxxxxxxx
MYSQL_PASSWORD=xxxxxxxxxxxxxxx
" \
> "$ENV_FILE"
fi

CONFIG_FILE=src/main/resources/batch_service_config.json
if [ ! -f "$CONFIG_FILE" ]; then
  echo \
"{
   \"samplesOrigin\": \"xai_sentences\",
   \"batchSize\": 99,
   \"targetExpertAnswerCount\": 0,
   \"targetAnswerCount\": 1,
   \"prioritizedReferenceSentences\": [],
   \"prioritizedQuestions\": []
 }
" \
> "$CONFIG_FILE"
fi