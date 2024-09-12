#!/bin/bash
# shellcheck disable=SC2164 disable=SC2086 disable=SC1090
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd)
cd "$SHELL_FOLDER"

bash ../build_tpl.sh "netty-server-tcp" \
  "netty-server-tcp-1.0-all.jar" \
  "iproute/netty-server-tcp"