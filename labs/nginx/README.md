# NGINX Config

```bash

# reverse Nginx Proxy Manager
# Delete site
# rm /etc/nginx/sites-enabled/proxy.labs-nv.duckdns.org
# rm /etc/nginx/sites-available/proxy.labs-nv.duckdns.org

vi /etc/nginx/sites-available/proxy.labs-nv.duckdns.org

server {
    listen 80;
    server_name proxy.labs-nv.duckdns.org;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name proxy.labs-nv.duckdns.org;

    ssl_certificate /etc/letsencrypt/live/labs-nv.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/labs-nv.duckdns.org/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://tailscale-nginx-proxy-manager:81;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

ln -s /etc/nginx/sites-available/proxy.labs-nv.duckdns.org /etc/nginx/sites-enabled/

nginx -t

systemctl restart nginx

# Test order container

# rm /etc/nginx/sites-enabled/test.labs-nv.duckdns.org
# rm /etc/nginx/sites-available/test.labs-nv.duckdns.org

vi /etc/nginx/sites-available/test.labs-nv.duckdns.org

server {
    listen 80;
    server_name test.labs-nv.duckdns.org;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name test.labs-nv.duckdns.org;

    ssl_certificate /etc/letsencrypt/live/labs-nv.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/labs-nv.duckdns.org/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://test.labs.nv;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

ln -s /etc/nginx/sites-available/test.labs-nv.duckdns.org /etc/nginx/sites-enabled/

nginx -t

systemctl restart nginx

# update default for reject request

vi /etc/nginx/sites-available/default 

server {
    listen 80 default_server;
    listen 443 ssl default_server;
    server_name _;
    ssl_certificate /etc/letsencrypt/live/labs-nv.duckdns.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/labs-nv.duckdns.org/privkey.pem;
    return 444;
}

nginx -t

systemctl restart nginx

```
