This script generates an SSL certificate for a given Webid in the context of an NGINX webserver.

Precondition: 

1. nginx is setup and running
2. wildcard DNS is configured for your domain
3. certbot-auto is installed
2. copy webid-proxy-config-template to nginx config path (usually /etc/nginx)

Mandatory Script paramters:

1. Username (e.g. markus)
2. Domain (e.g. jolocom.de)
3. Path of certbot-auto (e.g. /root)

Run:

./sh generate-ssl-for-webid.sh markus jolocom.de /root

to generate certificate for

https://markus.webid.jolocom.de

Warning: 

Use this script at your own risk. There is no validation of paramters (yet).
