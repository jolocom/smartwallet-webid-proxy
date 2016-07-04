This script generates an SSL certificate for a given Webid in the context of a NGINX webserver.

Precondition: 

1. nginx is setup and running
2. wildcard DNS is configured for your domain
3. certbot-auto is installed
2. copy webid-proxy-config-template to nginx config path (usually /etc/nginx)

Script paramters:

OPTIONS
  -u username
     the WebId username (e.g. markus)
  -d domain
     the WebId root domain (e.g. jolocom.de)
  -c certbot
     the path to certbot executable (default: /usr/bin/certbot)
  -w webrootpath
     the path to webroot (default: /usr/share/nginx/html)
  -q 
     execute script without confirmation quietly
  -h 
     show this help

Example: 

./bash generate-ssl-for-webid.sh -u markus -d jolocom.de -c /root/certbot-auto -q

to quietly generate nginx config with SSL certificate for WebID

https://markus.webid.jolocom.de

Warning: 

Use this script at your own risk. There is no validation of paramters (yet).
