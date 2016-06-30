#!/bin/bash
user=$1
domain=$2
certbot_auto_path=$3
webid=$user.webid.$domain

sed -e "s/_USER_/$user/g; s/_DOMAIN_/$domain/g;" webid-user-config-template > /etc/nginx/sites-available/$webid

ln -s  /etc/nginx/sites-available/$webid /etc/nginx/sites-enabled/$webid

/etc/init.d/nginx reload

$certbot_auto_path/certbot-auto certonly --webroot --webroot-path /usr/share/nginx/html/ -d $webid -q

sed -e "s/_USER_/$user/g; s/_DOMAIN_/$domain/g;" webid-user-ssl-config-template > /etc/nginx/sites-available/$webid

/etc/init.d/nginx reload

