# Cockroach

```shell
cockroach start --insecure --store=path=node1,size=640Mib --listen-addr=localhost:26257 --http-addr=localhost:8080 --join='localhost:26257, localhost:26258'

cockroach start --insecure --store=path=node2,size=640Mib --listen-addr=localhost:26258 --http-addr=localhost:8081 --join='localhost:26257, localhost:26258'

cockroach start --insecure --store=path=node3,size=640Mib --listen-addr=localhost:26259 --http-addr=localhost:8082 --join='localhost:26257, localhost:26258, localhost:26259'

cockroach init --host localhost:26257 --insecure
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
cockroach sql --host localhost:26257 --insecure < create.sql
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
    dsn: postgresql://root@localhost:26257/defaultdb?sslmode=disable
    table: product
    columns: [ name, title, description, url, sku ]
    args_mapping: |
      root = [ name, title, description, url, sku ]
```

```shell
benthos -c generate.yaml

cockroach start --insecure --store=path=node4,size=640Mib --listen-addr=localhost:26260 --http-addr=localhost:8083 --join='localhost:26257, localhost:26258, localhost:26259'

```

