#!/bin/bash

cur="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
tmp=`mktemp -d`
nginxconfpath=$tmp/nginx
letsencryptconfpath=$tmp/letsencrypt
soliddistpath=$tmp/solid/dist
webidproxyuserspath=$tmp/webid-proxy/users
webiddomain=markus.web.id

##############
### HELPER ###
##############

function file() {
  if [ $2 == should_be ]; then
    if [ ! -e $1 ]; then
      failure "File does not exist $1"
    fi
  else [ $2 == should_not_be ]
    if [ -e $1 ]; then
      failure "File exists $1"
    fi
  fi
}

function folder() {
  if [ $2 == should_be ]; then
    if [ ! -d $1 ]; then
      failure "Folder does not exist $1"
    fi
  else [ $2 == should_not_be ]
    if [ -d $1 ]; then
      failure "Folder exists $1"
    fi
  fi
} 

function failure() {
  echo -e "\e[31m\e[1m[FAIL] $1\e[0m" >&2
  exit 1
}

function setup() {
  setup_nginx
  setup_letsencrypt
  setup_solid
  setup_webid_proxy
}


function setdown() {
  rm -rf $tmp
}
function setup_nginx() {
  mkdir -p $nginxconfpath
  for f in sites-available sites-enabled; do
    mkdir -p $nginxconfpath/$f && touch $_/$webiddomain
  done
}

function setup_letsencrypt() {
  mkdir -p $letsencryptconfpath
  for f in live archive; do
    mkdir -p $letsencryptconfpath/$f/$webiddomain
  done
  mkdir -p $letsencryptconfpath/renewal && touch $_/${webiddomain}.conf
}

function setup_solid() {
  mkdir -p $soliddistpath/$webiddomain
}

function setup_webid_proxy() {
  mkdir -p $webidproxyuserspath
  touch $webidproxyuserspath/markus
  touch $webidproxyuserspath/markus.p12
}


##############
### SCRIPT ###
##############

setup

file $tmp/nginx/sites-available/markus.web.id should_be
file $tmp/nginx/sites-enabled/markus.web.id should_be
folder $tmp/letsencrypt/live/markus.web.id should_be
folder $tmp/letsencrypt/archive/markus.web.id should_be
file $tmp/letsencrypt/renewal/markus.web.id.conf should_be
folder $tmp/solid/dist/markus.web.id should_be
file $tmp/webid-proxy/users/markus should_be
file $tmp/webid-proxy/users/markus.p12 should_be

bash $cur/../remove.sh -q -w markus.web.id -n $tmp/nginx -l $tmp/letsencrypt -p $tmp/webid-proxy/users -s $tmp/solid/dist >/dev/null 2>/dev/null

file $tmp/nginx/sites-available/markus.web.id should_not_be
file $tmp/nginx/sites-enabled/markus.web.id should_not_be
folder $tmp/letsencrypt/live/markus.web.id should_not_be
folder $tmp/letsencrypt/archive/markus.web.id should_not_be
file $tmp/letsencrypt/renewal/markus.web.id.conf should_not_be
folder $tmp/solid/dist/markus.web.id should_not_be
file $tmp/webid-proxy/users/markus should_not_be
file $tmp/webid-proxy/users/markus.p12 should_not_be

setdown
