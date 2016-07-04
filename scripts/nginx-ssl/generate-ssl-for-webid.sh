#!/bin/bash

###############
### HELPERS ###
###############
function preamble() {
  echo -ne "\e[91m"
  echo "THERE IS NO WARRANTY FOR THIS PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW."
  echo "EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER"
  echo "PARTIES PROVIDE THE PROGRAM \"AS IS\" WITHOUT WARRANTY OF ANY KIND, EITHER"
  echo "EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF"
  echo "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE"
  echo "QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE"
  echo "DEFECTIVE, YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION."
  echo "PROVIDING THE WRONG PARAMTERS MAY BE HARMFUL TO YOUR SYSTEM. BE CAREFUL!"
  echo -e "\e[39m"

  if [ $opt_quiet = false ]; then
    echo -n "Continue? (yes/no) "
    read confirm && echo

    if [ "${confirm}" != yes ]; then
      exit_error "Aborted"
    fi
  fi
}

function remove_file() {
 if [ -f $1 -o -h $1 ]; then
   echo "Removing ${1}"
   rm $1
 else
   echo "Tried to remove ${1} but does not exist!"
 fi
}

#################
### FUNCTIONS ###
#################
function error() {
  cleaning_exit

  exit_error "Command in ${1}:${2} failed."
}

function cleaning_exit() {
 remove_file "/etc/nginx/sites-available/${webid}"

 remove_file "/etc/nginx/sites-enabled/${webid}"

 #TODO: clean up certificate
}


function check_args() {
  if [ -z $opt_user ]; then
    exit_usage "Username is missing"
  elif [[ ! "$opt_user" =~ ^[0-9a-z]{3,10}$ ]]; then
    exit_usage "Username must may only contain 3-10 alpha numeric lowercase characters"
  fi

  if [ -z $opt_domain ]; then
    exit_usage "Domain is missing"
  else
    # http://stackoverflow.com/a/32910760
    if [[ -z `echo $opt_domain | grep -P '(?=^.{1,254}$)(^(?>(?!\d+\.)[a-zA-Z0-9_\-]{1,63}\.?)+(?:[a-zA-Z]{2,})$)'` ]]; then
      exit_usage "Not a valid domain"
    fi
  fi

  if [ ! -e $opt_certbot ]; then
    exit_usage "Certbot not found ($opt_certbot)"

    if [ ! -x $opt_certbot ]; then
      exit_usage "Certbot not executable ($opt_certbot)"
    fi
  fi

  if [ ! -d $opt_webrootpath ]; then
    exit_usage "Webroot path does not exist"
  fi
}

function set_webid() {
  webid="${opt_user}.webid.${opt_domain}"
}

function set_nginx_config() {
 nginx_conf_available="/etc/nginx/sites-available/${webid}"

 if [ -f $nginx_conf_available ]; then
   exit_error "Nginx config already exists ${nginx_conf_available}"
 fi

 nginx_conf_enabled="/etc/nginx/sites-enabled/${webid}"

 if [ -f $nginx_conf_enabled ]; then
   exit_error "Nginx config already exists ${nginx_conf_enabled}"
 fi
}

function create_nginx_config() {
  sed -e "s/_USER_/$opt_user/g; s/_DOMAIN_/$opt_domain/g;" webid-user-config-template > $nginx_conf_available
  ln -s  $nginx_conf_available $nginx_conf_enabled
}

function generate_certificate() {
  $opt_certbot certonly --webroot --webroot-path $opt_webrootpath -d $webid -q
}

function update_nginx_config() {
  sed -e "s/_USER_/$opt_user/g; s/_DOMAIN_/$opt_domain/g;" webid-user-ssl-config-template > $nginx_conf_available
}

function reload_nginx() {
  /etc/init.d/nginx reload
}

function show_usage() {
  echo -e "\e[1mOPTIONS\e[0m" >&2
  echo -e "  \e[1m-u\e[0m \e[4musername\e[0m" >&2 
  echo -e "     the WebId username (e.g. markus)" >&2
  echo -e "  \e[1m-d\e[0m \e[4mdomain\e[0m" >&2 
  echo -e "     the WebId root domain (e.g. jolocom.de)" >&2
  echo -e "  \e[1m-c\e[0m \e[4mcertbot\e[0m" >&2 
  echo -e "     the path to certbot executable (default: $certbot_default)" >&2
  echo -e "  \e[1m-w\e[0m \e[4mwebrootpath\e[0m" >&2 
  echo -e "     the path to webroot (default: $webrootpath_default)" >&2
  echo -e "  \e[1m-q\e[0m \e[4m\e[0m" >&2 
  echo -e "     execute script without confirmation quietly" >&2
  echo -e "  \e[1m-h\e[0m \e[0m" >&2 
  echo -e "     show this help" >&2
}

function exit_usage() {
  exit_error "${1}" usage
}

function exit_error() {
  if [ ! -z "${1}" ]; then
    echo -e "\e[31m\e[1m[ERR] $1\e[0m" >&2
  fi

  if [ "${2}" == usage ]; then
    if [ -z "${1}" ]; then
      echo -e "\n       \e[7m\e[1m WebID Proxy Nginx/SSL generator \e[0m\n"
    else
      echo
    fi

    show_usage
  fi

  exit 1
}

########################
### GLOBAL VARIABLES ###
########################
certbot_default=/usr/bin/certbot
webrootpath_default=/usr/share/nginx/html
opt_certbot=$certbot_default
opt_webrootpath=$webrootpath_default
opt_quiet=false

##############
### SCRIPT ###
##############

trap 'error "${BASH_SOURCE}" "${LINENO}"' ERR

while getopts "u:d:c:w:qh" opt; do
  case $opt in
    u) opt_user=$OPTARG ;;
    d) opt_domain=$OPTARG ;;
    c) opt_certbot=$OPTARG ;;
    w) opt_webrootpath=$OPTARG ;;
    q) opt_quiet=true ;;
    h) exit_usage ;;
    \?) exit_usage ;;
  esac
done

preamble

check_args

set_webid

set_nginx_config

create_nginx_config

reload_nginx

generate_certificate

update_nginx_config

reload_nginx
