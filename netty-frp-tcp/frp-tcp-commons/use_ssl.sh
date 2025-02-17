#!/bin/bash
# shellcheck disable=SC1090 disable=SC2086 disable=SC2155 disable=SC2128 disable=SC2028 disable=SC2164
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd) && cd "$SHELL_FOLDER"
[ -z $ROOT_URI ] && source <(curl -sSL https://gitlab.com/iprt/shell-basic/-/raw/main/build-project/basic.sh)
echo -e "\033[0;32mROOT_URI=$ROOT_URI\033[0m"
# ROOT_URI=https://dev.kubectl.net

source <(curl -SL $ROOT_URI/func/log.sh)

bash src/main/resources/ssl.sh
