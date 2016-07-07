#!/bin/bash

cur="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
tmp=`mktemp -d`

webid=Markus.webid.jolocom.de
nginxconf=$tmp/nginx
webroot=$tmp/webroot

#################
### VARIABLES ###
#################
any_failures=false

###############
### HELPERS ###
###############

function setup() {
  mkdir $nginxconf
  mkdir $webroot
}


function setdown() {
  rm -rf $tmp
  test_result
}

function test_result() {
  if [ $any_failures = false ]; then
    echo -e "\e[1m[SUCCESS]\e[0m"
  fi
}

function test_failed() {
  any_failures=true
  echo -e "\e[91m\e[1m[FAILED] ${1} \e[0m"
}

#############
### TESTS ###
#############

function validate_nginx_conf_files() {

  f="${nginxconf}/sites-available/markus.webid.jolocom.de"
  if [ ! -e $f ]; then
    test_failed "File ${f} is missing"
  fi

  l="${nginxconf}/sites-enabled/markus.webid.jolocom.de"
  if [ ! -h $l ]; then
    test_failed "Symbolic link ${l} is missing"
  fi
}

##############
### SCRIPT ###
##############

setup

bash $cur/generate-nginx-ssl-for-webid-stubbed.sh -i $webid -n $nginxconf -w $webroot -q

validate_nginx_conf_files

setdown
