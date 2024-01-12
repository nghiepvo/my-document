```shell
# bitnami/postgresql-ha
helm install postgresql-ha bitnami/postgresql-ha --values values.yaml -n postgres --create-namespace

export POSTGRES_PASSWORD=$(kubectl get secret --namespace postgres postgresql-ha-postgresql -o jsonpath="{.data.password}" | base64 -d)

export REPMGR_PASSWORD=$(kubectl get secret --namespace postgres postgresql-ha-postgresql -o jsonpath="{.data.repmgr-password}" | base64 -d)

kubectl run postgresql-ha-client --rm --tty -i --restart='Never' --namespace postgres --image docker.io/bitnami/postgresql-repmgr:15.4.0-debian-11-r31 --env="PGPASSWORD=$POSTGRES_PASSWORD"  \
        --command -- psql -h postgresql-ha-pgpool -p 5432 -U postgres -c "select inet_server_addr();"
```

# Setup Postgres with SSL

```shell
apt update && apt upgrade -y

apt-get install wget sudo curl gnupg2 -y

apt -y update

apt-get install postgresql-15 -y

su - postgres

mkdir ssl && cd ssl

openssl req -new -nodes -text -out ca.csr -keyout ca-key.pem -subj "/CN=certificate-authority"

openssl x509 -req -in ca.csr -text -extfile /etc/ssl/openssl.cnf -extensions v3_ca -signkey ca-key.pem -out ca-cert.pem

openssl req -new -nodes -text -out server.csr -keyout server-key.pem -subj "/CN=pg-server"

openssl x509 -req -in server.csr -text -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out server-cert.pem

openssl req -new -nodes -text -out client.csr -keyout client-key.pem -subj "/CN=pg-client"

openssl x509 -req -in client.csr -text -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out client-cert.pem

ls

mkdir -p /etc/ssl/postgresql/

cp ca-cert.pem server-cert.pem server-key.pem /etc/ssl/postgresql/
chmod -R 700 /etc/ssl/postgresql
chown -R postgres.postgres /etc/ssl/postgresql


vi /etc/postgresql/15/main/postgresql.conf

# Make sure the server listen to it's own IP to allow remote connections
# Replace `SERVER_PUBLIC_IP` with the server public IP address
listen_addresses = 'localhost,SERVER_PUBLIC_IP'

# SSL should already be `on`, switch it to `on` if its not the case
ssl = on # <-- This should be "on" by default

# Set SSL certificate
ssl_cert_file = '/etc/ssl/postgresql/server-cert.pem'
ssl_key_file = '/etc/ssl/postgresql/server-key.pem'
ssl_ca_file = '/etc/ssl/postgresql/ca-cert.pem'


vi /etc/postgresql/15/main/pg_hba.conf


host    all             all             162.43.28.136/32        scram-sha-256
hostssl all             all             0.0.0.0/0               scram-sha-256 clientcert=verify-ca



# copy file 

scp root@host:/var/lib/postgresql/ssl/{client-cert.pem,client-key.pem,ca-cert.pem} ./


su - postgres

psql
ALTER USER user_name WITH PASSWORD 'new_password';
```