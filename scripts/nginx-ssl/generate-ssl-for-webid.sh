#!/bin/bash


#################
### FUNCTIONS ###
#################

function set_webid() {
  webid="${user}.webid.${domain}"
}

function create_nginx_config() {
  sed -e "s/_USER_/$user/g; s/_DOMAIN_/$domain/g;" webid-user-config-template > /etc/nginx/sites-available/$webid
  ln -s  /etc/nginx/sites-available/$webid /etc/nginx/sites-enabled/$webid
}

function generate_certificate() {
  $certbot_auto_path/certbot-auto certonly --webroot --webroot-path /usr/share/nginx/html/ -d $webid -q
}

function update_nginx_config() {
  sed -e "s/_USER_/$user/g; s/_DOMAIN_/$domain/g;" webid-user-ssl-config-template > /etc/nginx/sites-available/$webid
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
  echo -e "  \e[1m-p\e[0m \e[4mpath\e[0m" >&2 
  echo -e "     the certbot path (e.g. /root)" >&2
  echo -e "  \e[1m-h\e[0m \e[0m" >&2 
  echo -e "     show this help" >&2
}

function exit_usage() {
  show_usage
  exit 1
}

##############
### SCRIPT ###
##############

while getopts "u:d:p:h" opt; do
  case $opt in
    u) user=$OPTARG ;;
    d) domain=$OPTARG ;;
    p) certbot_auto_path=$OPTARG ;;
    h) exit_usage ;;
    \?) exit_usage ;;
  esac
done

set_webid

create_nginx_config

reload_nginx

generate_certificate

update_nginx_config

reload_nginx
