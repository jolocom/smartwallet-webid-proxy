This script generates an SSL certificate for a given Webid in the context of a NGINX webserver.

Precondition: 

1. nginx is setup and running
2. wildcard DNS is configured for your domain
3. certbot-auto is installed
4. copy webid-proxy-config-template to nginx config path (usually /etc/nginx)

Script paramters:

Please run `bash generate-nginx-ssl-for-webid.sh -h` to see all paramters.

Warning: 

Use this script at your own risk. There is no validation of paramters (yet).
