#!/bin/bash
# shellcheck disable=SC2164 disable=SC1090
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd)
cd "$SHELL_FOLDER"

resource_dir="frp-tcp-commons/src/main/resources"

ca_file="ssl/ca.crt"

if [ ! -f $resource_dir/$ca_file ]; then
  echo "ca file not found: $resource_dir/$ca_file"
  exit 1
fi

openssl s_client -connect localhost:7000 -showcerts \
  -CAfile $resource_dir/ssl/ca.crt \
  -cert $resource_dir/ssl/client/client.crt \
  -key $resource_dir/ssl/client/client.key

# ctrl + c close
