#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR" || exit 1

ENV_FILE=.env
if [ ! -f "$ENV_FILE" ]; then
  echo \
"APP_URL_ROOT=http://localhost:3000

MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=handwriting_labeling_app
MYSQL_USERNAME=xxxxxxxxxxxxxxx
MYSQL_PASSWORD=xxxxxxxxxxxxxxx
" \
> "$ENV_FILE"
fi