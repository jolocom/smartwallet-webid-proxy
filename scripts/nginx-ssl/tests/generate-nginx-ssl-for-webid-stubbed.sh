#!/bin/bash

cur="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source "${cur}/../_def_"

function stubbed_hint() {
  echo -e "\e[1m[HINT]\e[0m Call to \e[4m${1}\e[24m is stubbed!"
}

function reload_nginx() {
  stubbed_hint "reload_nginx"
}

function generate_certificate() {
  stubbed_hint "generate_certificate"
}

source "${cur}/../_exec_"
