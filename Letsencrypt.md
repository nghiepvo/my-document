```shell
# certbot certonly --manual --preferred-challenges=dns --email nghiepvo@ohmidasvn.com --server https://acme-v02.api.letsencrypt.org/directory --agree-tos -d *.mainichi-nihongo.net -d mainichi-nihongo.net

# update _acme-challenge on register domain


#- Congratulations! Your certificate and chain have been saved at:
#   /etc/letsencrypt/live/mainichi-nihongo.net/fullchain.pem
#   Your key file has been saved at:
#   /etc/letsencrypt/live/mainichi-nihongo.net/privkey.pem
#   Your certificate will expire on 2023-11-13. To obtain a new or
#   tweaked version of this certificate in the future, simply run
#   certbot again. To non-interactively renew *all* of your
#   certificates, run "certbot renew"
# - If you like Certbot, please consider supporting our work by:

#   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
#   Donating to EFF:                    https://eff.org/donate-le
   

cd /etc/letsencrypt/live/mainichi-nihongo.net

openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out mainichi-nihongo.net.pfx

cd /etc/letsencrypt/live/mainichi-nihongo.net

certbot renew
```
