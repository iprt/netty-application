#!/bin/bash
# shellcheck disable=SC2164 disable=SC2086  disable=SC1090
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd)
cd "$SHELL_FOLDER"

source <(curl -SL https://gitlab.com/iprt/shell-basic/-/raw/main/build-project/basic.sh)
source <(curl -sSL $ROOT_URI/func/log.sh)

log_info "build netty-server-socks"
bash netty-server-socks/build.sh

log_info "build netty-server-tcp"
bash netty-server-tcp/build.sh
