```shell
cd private-registry
mkdir registry-data
mkdir auth

cd auth

htpasswd -Bc registry.password admin
```

## Generating certificate/key pair for your private Docker registry
```shell
mkdir certs

vi openssl.conf
```

Copy below content to __openssl.conf__

_Update **Docker Server IP** with the IP address of your server where you will be running docker registry_
```shell
[ req ]
distinguished_name = req_distinguished_name
x509_extensions     = req_ext
default_md         = sha256
prompt             = no
encrypt_key        = no

[ req_distinguished_name ]
countryName            = "GB"
localityName           = "London"
organizationName       = "Just Me and Opensource"
organizationalUnitName = "YouTube"
commonName             = "<Docker Server IP>"
emailAddress           = "test@example.com"

[ req_ext ]
subjectAltName = "IP:<Docker Server IP>"

[alt_names]
DNS = "<Docker Server IP>"
```
Generate the certificate and private key

```shell
openssl req \
 -x509 -newkey rsa:4096 -days 365 -config openssl.conf \
 -keyout certs/domain.key -out certs/domain.crt
```
To verify your certificate
```shell
openssl x509 -text -noout -in certs/domain.crt
```


```shell

cd ..
docker compose up -d

# register certificate on client
scp ~./private-registry/certs/domain.crt /usr/share/ca-certificates/

dpkg-reconfigure ca-certificates

update-ca-certificates



docker tag busybox docker.ohmidasvn.dev/jupyterlab

docker image rmi docker.ohmidasvn.dev/jupyterlab

docker push docker.ohmidasvn.dev/jupyterlab

curl -X GET -u "admin:Mypassword" https://docker.ohmidasvn.dev/v2/_catalog

curl -s GET -u "admin:Mypassword" https://docker.ohmidasvn.dev/v2/jupyterlab/tags/list

# delete repo
curl -sS -u "admin:Mypassword" https://docker.ohmidasvn.dev/v2/jupyterlab/manifests/latest -H 'Accept: application/vnd.docker.distribution.manifest.v2+json' -w '%header{Docker-Content-Digest}\n' -o /dev/null 

curl -sS -X DELETE -u "admin:Mypassword" https://docker.ohmidasvn.dev/v2/jupyterlab/manifests/sha256:023917ec6a886d0e8e15f28fb543515a5fcd8d938edb091e8147db4efed388ee

# allow connect private registry with insecure

vi /etc/docker/daemon.json
```

```JSON
{ "insecure-registries":["192.168.1.164:5000"] }
```
```shell

systemctl restart docker

# install on k0s
kubectl create secret docker-registry docker-cert --dry-run=client --docker-server=imgs.ohmidasvn.dev --docker-username=admin --docker-password=Mypassword -o yaml > docker-cert.yaml
kubectl apply -f deployment.yaml
```


