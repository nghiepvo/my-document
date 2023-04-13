
# Cockroach

```shell
mkdir certs my-safe-directory

cockroach cert create-ca --certs-dir=certs --ca-key=my-safe-directory/ca.key

cockroach cert create-node localhost $(hostname) --certs-dir=certs --ca-key=my-safe-directory/ca.key

cockroach cert create-client root --certs-dir=certs --ca-key=my-safe-directory/ca.key

cockroach start --certs-dir=certs --store=path=node1,size=640Mib --listen-addr=localhost:26257 --http-addr=localhost:8080 --join='localhost:26257, localhost:26258, localhost:26259, localhost:26260' --background

cockroach start --certs-dir=certs --store=path=node2,size=640Mib --listen-addr=localhost:26258 --http-addr=localhost:8081 --join='localhost:26257, localhost:26258, localhost:26259, localhost:26260' --background

cockroach start --certs-dir=certs --store=path=node3,size=640Mib --listen-addr=localhost:26259 --http-addr=localhost:8082 --join='localhost:26257, localhost:26258, localhost:26259, localhost:26260' --background

cockroach init --host localhost:26257 --certs-dir=certs

cockroach sql --certs-dir=certs --host=localhost:26257

grep 'node starting' node1/logs/cockroach.log -A 11

CREATE USER nv WITH PASSWORD 'nv';

GRANT admin TO nv;
```

Create a product.sql file

```sql
CREATE TABLE product (
    "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" STRING NOT NULL,
    "title" STRING NOT NULL,
    "description" STRING NOT NULL,
    "url" STRING NOT NULL,
    "sku" STRING NOT NULL
)
```

```shell
cockroach sql --url 'postgresql://root@localhost:26257/defaultdb?sslcert=/home/nv/certs/client.root.crt&sslkey=/home/nv/certs/client.root.key&sslmode=verify-full&sslrootcert=/home/nv/certs/ca.crt' < create.sql
```

## Benthos

<https://www.benthos.dev/>

```shell
curl -Lsf https://sh.benthos.dev | bash
```

Create a generate.yaml file

```yaml
input:
  generate:
    mapping: |
      root.name = fake("word")
      root.title = fake("sentence")
      root.description = fake("paragraph")
      root.url = fake("url")
      root.sku = fake("password")
    interval: 0s
    count: 100000
output:
  sql_insert:
    driver: postgres
    dsn: postgresql://localhost:26257/defaultdb?sslcert=/home/nv/certs/client.root.crt&sslkey=/home/nv/certs/client.root.key&sslmode=verify-full&sslrootcert=/home/nv/certs/ca.crt&user=root
    table: product
    columns: [ name, title, description, url, sku ]
    args_mapping: |
      root = [ name, title, description, url, sku ]
```

```shell
# connection with ssl mode
# jdbc:postgresql://localhost:26257/defaultdb?sslcert=/home/nv/certs/client.root.crt&sslkey=/home/nv/certs/client.root.key&sslmode=verify-full&sslrootcert=/home/nv/certs/ca.crt&user=root

# SSL mode = verify-full
# CA Certificate: /home/nv/certs/ca.crt
# Client Certificate: /home/nv/certs/client.root.crt
# Client Private Key: /home/nv/certs/client.root.key

benthos -c generate.yaml

cockroach start --certs-dir=certs--store=path=node4,size=640Mib --listen-addr=localhost:26260 --http-addr=localhost:8083 --join='localhost:26257, localhost:26258, localhost:26259, localhost:26260' --background

```

