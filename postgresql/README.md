```shell
# bitnami/postgresql-ha
helm install postgresql-ha bitnami/postgresql-ha --values values.yaml -n postgres --create-namespace

export POSTGRES_PASSWORD=$(kubectl get secret --namespace postgres postgresql-ha-postgresql -o jsonpath="{.data.password}" | base64 -d)

export REPMGR_PASSWORD=$(kubectl get secret --namespace postgres postgresql-ha-postgresql -o jsonpath="{.data.repmgr-password}" | base64 -d)

kubectl run postgresql-ha-client --rm --tty -i --restart='Never' --namespace postgres --image docker.io/bitnami/postgresql-repmgr:15.4.0-debian-11-r31 --env="PGPASSWORD=$POSTGRES_PASSWORD"  \
        --command -- psql -h postgresql-ha-pgpool -p 5432 -U postgres -c "select inet_server_addr();"
```