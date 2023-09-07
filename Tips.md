# Tips

Use **Terminator** application on Linux.  

**mysql client:** use ```Ctrl + L``` or ```\! cls``` for clear screen LOL.  

## vim

### Copy and parse

**Shift-v** starts visual mode and selects current line  
**j** or arrow next. Visual mode movement 3 lines below current line  
**y** Yanks(copies) the selected stuff in the register  
**p** Paste the contents after current line.  

**/ or ?** enter the search pattern and hit Enter.  

## Fix perl: warning

```shell
export LANGUAGE=en_US.UTF-8
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
sudo locale-gen vi_VN.UTF-8
sudo locale-gen en_US.UTF-8 
locale-gen en_US.UTF-8
dpkg-reconfigure locales
```

## View stored postgresql DB

```shell
psql
SELECT oid from pg_database where datname = 'dbName';

ls -lrha /var/lib/postgresql/15/main/base/
```

## find file

```shell
find / -name filename
```

## change PASS

```shell
passwd username
```

## setup sudo and allow user run sudo without password

```shell
apt install sudo -y

chmod 0755 /etc/sudoers
```

```conf
vi /etc/sudoers
# add end of line
username     ALL=(ALL) NOPASSWD:ALL
```

```shell

chmod 0755 /etc/sudoers
```

## set variable environment all user

```conf
vi /etc/profile
...
export PGDATA="/var/lib/postgresql/15/data"
export ETCDCTL_API="3"
export PATRONI_ETCD_URL="http://127.0.0.1:2379"
export PATRONI_SCOPE="pg_cluster"
patroni-1=192.168.1.181
patroni-2=192.168.1.182
patroni-3=192.168.1.183
ENDPOINTS=$patroni1:2379,$patroni2:2379,$patroni3:2379
```

## Kill port in linux

```shell
sudo netstat -tulpn | grep LISTEN
sudo lsof -t -i tcp:8080

sudo kill $(sudo lsof -t -i tcp:8080)
```

## Mount to proxmox container disk
```shell
find /dev | grep 104

kpartx -a /dev/NVME1T/vm-104-disk-0

mount /dev/mapper/NVME1T-vm--104--disk--0 /mnt

umount /mnt

```

## Export ssl from Letencrypt

```shell
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
   
openssl x509 -outform der -in fullchain.pem -out mainichi-nihongo.net.crt

openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out mainichi-nihongo.net.pfx

openssl pkcs12 -export -in cert.pem -inkey privkey.pem -out mainichi-nihongo.net.pfx

openssl pkcs12 -inkey privkey.pem.pem -in bob_cert.cert -export -out bob_pfx.pfx
```

## Export SSL  from browser and upload on server CI/CD

```shell
sudo openssl x509 -inform DER -in certificate.cer -out certificate.crt

sudo mv certificate.crt /usr/share/ca-certificate/

cd /usr/share/ca-certificate

sudo chmod 644 certificate.crt

sudo dpkg-reconfigure ca-certificates

sudo update-ca-certificates
```
## Fix lỗi không cài đc mysql on debian
```shell
apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 467B942D3A79BD29
```
