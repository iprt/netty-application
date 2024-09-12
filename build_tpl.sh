#!/bin/bash
# shellcheck disable=SC2164  disable=SC1090 disable=SC2086
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd)
cd "$SHELL_FOLDER"

source <(curl -SL https://gitlab.com/iprt/shell-basic/-/raw/main/build-project/basic.sh)
source <(curl -sSL $ROOT_URI/func/log.sh)

sub_project_path="$1"
sub_project_jar_name="$2"
sub_project_image_name="$3"

log_info "step 1" "gradle build jar"

sub_project_gradle_path=$(echo "$sub_project_path" | sed 's/\//:/g')
bash <(curl $ROOT_URI/gradle/build.sh) \
  -i "registry.cn-shanghai.aliyuncs.com/iproute/gradle:8.4-jdk17" \
  -c "gradle_8.4-jdk17_cache" \
  -x "gradle clean $sub_project_gradle_path:build -x test"

jar_name="$sub_project_jar_name"

if [ ! -f "$sub_project_path/build/libs/$jar_name" ]; then
  log_error "validate" "$jar_name 不存在，打包失败，退出"
  exit 1
fi

log_info "step 2" "docker build and push"

registry="registry.cn-shanghai.aliyuncs.com"

#timestamp_tag=$(date +"%Y-%m-%d_%H-%M-%S")
version="latest"
#version="$(date '+%Y%m%d')_$(git rev-parse --short HEAD)"

bash <(curl $ROOT_URI/docker/build.sh) \
  -f "$sub_project_path/Dockerfile" \
  -i "$registry/$sub_project_image_name" \
  -v "$version" \
  -r "false" \
  -p "true"
