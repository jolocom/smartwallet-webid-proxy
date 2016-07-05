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

function remove() {
  if [ $opt_quiet = false ]; then
    echo "Do you really want to delete ${1} .. (yes/no)"
    confirm=false
    read confirm && echo

    if [ "${confirm}" != yes ]; then
      rm -rf $1 2>/dev/null
    fi
  else
    echo "Deleting .. ${1}"
    rm -rf $1 2>/dev/null
  fi
}

#################
### FUNCTIONS ###
#################
function error() {
  exit_error "Command in ${1}:${2} failed."
}

function check_args() {
  if [ -z $opt_webiddomain ]; then
    exit_usage "WebId domain is missing"
  else
    # http://stackoverflow.com/a/32910760
    if [[ -z `echo $opt_webiddomain | grep -P '(?=^.{1,254}$)(^(?>(?!\d+\.)[a-zA-Z0-9_\-]{1,63}\.?)+(?:[a-zA-Z]{2,})$)'` ]]; then
      exit_usage "Not a valid webid domain"
    fi
  fi

  if [ ! -d $opt_nginxconfpath ]; then
    exit_usage "Nginx config path does not exist"
  fi

  if [ ! -d $opt_letsencryptconfpath ]; then
    exit_usage "Let's Encrypt config path does not exist"
  fi

  if [ -z $opt_soliddistpath ]; then
    exit_usage "Solid dist path is missing"
  elif [ ! -d $opt_soliddistpath ]; then
    exit_usage "Solid dist path does not exist"
  fi

  if [ -z $opt_webidproxyuserspath ]; then
    exit_usage "WebID proxy users path is missing"
  elif [ ! -d $opt_webidproxyuserspath ]; then
    exit_usage "WebID proxy users path does not exist"
  fi
}

function remove_nginx() {
  remove "${opt_nginxconfpath}/sites-available/${opt_webiddomain}"
  remove "${opt_nginxconfpath}/sites-enabled/${opt_webiddomain}"
}

function reload_nginx() {
  /etc/init.d/nginx reload
}

function remove_letsencrypt() {
  remove "${opt_letsencryptconfpath}/live/${opt_webiddomain}"
  remove "${opt_letsencryptconfpath}/archive/${opt_webiddomain}"
  remove "${opt_letsencryptconfpath}/renewal/${opt_webiddomain}.conf"
}

function remove_solid() {
  remove "${opt_soliddistpath}/${opt_webiddomain}"
}

function remove_webid_proxy() {
  user=`expr "${opt_webiddomain}" : '\(^[a-z0-9][^\.]*\)'`
  remove "${opt_webidproxyuserspath}/${user}"
  remove "${opt_webidproxyuserspath}/${user}.p12"
}

function show_usage() {
  echo -e "\e[1mOPTIONS\e[0m" >&2
  echo -e "  \e[1m-n\e[0m \e[4mnginxconfpath\e[0m" >&2 
  echo -e "     the path to nginx config (default: $nginxconfpath_default)" >&2
  echo -e "  \e[1m-l\e[0m \e[4mletsencryptconfpath\e[0m" >&2 
  echo -e "     the path to letsencrypt config (default: $letsencryptconfpath_default)" >&2
  echo -e "  \e[1m-p\e[0m \e[4mwebidproxyuserspath\e[0m" >&2 
  echo -e "     the path to webid proxy users" >&2
  echo -e "  \e[1m-s\e[0m \e[4msoliddistpath\e[0m" >&2 
  echo -e "     the path to solid dist folder" >&2
  echo -e "  \e[1m-w\e[0m \e[4mwebiddomain\e[0m" >&2 
  echo -e "     the webid domain (e.g. markus.web.id)" >&2
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
      echo -e "\n       \e[7m\e[1m WebID Proxy Remover \e[0m\n"
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
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
nginxconfpath_default=/etc/nginx
letsencryptconfpath_default=/etc/letsencrypt

opt_nginxconfpath=$nginxconfpath_default
opt_letsencryptconfpath=$letsencryptconfpath_default

opt_quiet=false

##############
### SCRIPT ###
##############

# trap 'error "${BASH_SOURCE}" "${LINENO}"' ERR

while getopts "n:l:p:s:w:qh" opt; do

  case $opt in
    n) opt_nginxconfpath=$OPTARG ;;
    l) opt_letsencryptconfpath=$OPTARG ;;
    p) opt_webidproxyuserspath=$OPTARG ;;
    s) opt_soliddistpath=$OPTARG ;;
    w) opt_webiddomain=$OPTARG ;;
    q) opt_quiet=true ;;
    h) exit_usage ;;
    \?) exit_usage ;;
  esac
done

preamble

check_args

remove_nginx

remove_letsencrypt

remove_solid

remove_webid_proxy

reload_nginx
