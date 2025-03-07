#!/bin/bash
# shellcheck disable=SC1090 disable=SC2086 disable=SC2155 disable=SC2128 disable=SC2028 disable=SC2164
SHELL_FOLDER=$(cd "$(dirname "$0")" && pwd) && cd "$SHELL_FOLDER"
source <(curl -SL https://dev.kubectl.net/func/log.sh)

C=CN
ST=Shanghai
L=Shanghai
O=shanghai-electric
OU=shanghai-electric
# CN...
emailAddress="tech@intellij.io"

# different from the above
CA_CN="frp-ca"
SERVER_CN="frp-server"
SERVER_DNS=$SERVER_CN

CLIENT_CN="frp-client"

ssl_folder="ssl"

function init() {
  log_info "init" "init ssl folder"
  if [ -f "$ssl_folder" ]; then
    log_error "validate" "ssl folder is a file, please remove it first"
    exit 1
  fi
  if [ -d "$ssl_folder" ]; then
    log_warn "validate" "ssl folder is exist, remove it"
    rm -rf "$ssl_folder"
    mkdir -p "$ssl_folder"/{client,server}
  else
    log_info "validate" "ssl folder is not exist, create it"
    mkdir -p "$ssl_folder"/{client,server}
  fi
}

init

function detect_system_type() {
  # https://stackoverflow.com/questions/31506158/running-openssl-from-a-bash-script-on-windows-subject-does-not-start-with
  local OS=$(uname -s)
  case $OS in
  Linux)
    system_type="Linux"
    SUBJECT_PREFIX="/"
    ;;
  Darwin)
    system_type="Mac"
    SUBJECT_PREFIX="/"
    ;;
  CYGWIN* | MINGW32* | MSYS* | MINGW*)
    system_type="Windows"
    SUBJECT_PREFIX="//"
    ;;
  *)
    system_type="Unknown"
    ;;
  esac

  if [ $system_type == "Unknown" ]; then
    log_error "system_type" "Unknown system type"
    exit 1
  fi
}

detect_system_type

function create_ca() {
  log_info "ca" "create ca"

  function create_ca_key() {
    log_info "ca" "create ca key"
    openssl genrsa -out $ssl_folder/ca.key 2048
  }

  function create_ca_crt() {
    log_info "ca" "create ca certificate"
    local CA_SUBJ="${SUBJECT_PREFIX}C=${C}\ST=${ST}\L=${L}\O=${O}\OU=${OU}\emailAddress=${emailAddress}\CN=${CA_CN}"
    openssl req -x509 -sha256 -new -nodes -key $ssl_folder/ca.key -days 3650 -out $ssl_folder/ca.crt \
      -subj "${CA_SUBJ}"
  }

  create_ca_key
  create_ca_crt

  openssl x509 -in $ssl_folder/ca.crt -text
}

function create_server() {
  log_info "server" "create server"
  server_folder="$ssl_folder/server"
  function create_server_key() {
    log_info "server" "create server key"
    openssl genrsa -out $server_folder/server.key 2048
  }

  function create_server_csr() {
    log_info "server" "create server certificate signing request"
    local SERVER_SUBJ="${SUBJECT_PREFIX}C=${C}\ST=${ST}\L=${L}\O=${O}\OU=${OU}\emailAddress=${emailAddress}\CN=${SERVER_CN}"
    openssl req -new -key $server_folder/server.key -sha256 \
      -subj "$SERVER_SUBJ" \
      -out $ssl_folder/server/server.csr
  }

  function create_server_crt() {
    log_info "server" "create server certificate"
    cat >$server_folder/v3.ext.ini <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = $SERVER_DNS
EOF
    openssl x509 -req -sha256 \
      -in $server_folder/server.csr \
      -CA $ssl_folder/ca.crt -CAkey $ssl_folder/ca.key \
      -CAcreateserial \
      -out $server_folder/server.crt \
      -days 3650 \
      -extfile $server_folder/v3.ext.ini
  }

  create_server_key
  create_server_csr
  create_server_crt
  # openssl pkcs8 -topk8 -in $server_folder/server.key -out $server_folder/pkcs8_server.key -nocrypt
  openssl x509 -in $server_folder/server.crt -text
}

function create_client() {
  log_info "client" "create client"
  client_folder="$ssl_folder/client"

  function create_client_key() {
    log_info "server" "create client key"
    openssl genrsa -out $client_folder/client.key 2048
  }

  function create_client_csr() {
    log_info "server" "create client certificate signing request"
    local CLIENT_SUBJ="${SUBJECT_PREFIX}C=${C}\ST=${ST}\L=${L}\O=${O}\OU=${OU}\emailAddress=${emailAddress}\CN=${CLIENT_CN}"
    openssl req -new -key $client_folder/client.key -sha256 \
      -subj "$CLIENT_SUBJ" \
      -out $client_folder/client.csr
  }

  function create_client_crt() {
    log_info "server" "create client certificate"

    cat >$client_folder/v3.ext.ini <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = $CLIENT_CN
EOF
    openssl x509 -req -sha256 \
      -in $client_folder/client.csr \
      -CA $ssl_folder/ca.crt -CAkey $ssl_folder/ca.key \
      -CAcreateserial \
      -out $client_folder/client.crt \
      -days 3650 \
      -extfile $client_folder/v3.ext.ini
  }

  create_client_key
  create_client_csr
  create_client_crt
  openssl x509 -in $client_folder/client.crt -text

}

create_ca
create_server
create_client

function openssl_validate() {
  openssl verify -CAfile $ssl_folder/ca.crt $ssl_folder/server/server.crt
  openssl verify -CAfile $ssl_folder/ca.crt $ssl_folder/client/client.crt
}

openssl_validate
