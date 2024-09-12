#!/bin/bash
# shellcheck disable=SC2164
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd)
cd "$SHELL_FOLDER"

bash ../build_tpl.sh "netty-server-socks" \
  "netty-server-socks-1.0-all.jar" \
  "iproute/netty-server-socks"
