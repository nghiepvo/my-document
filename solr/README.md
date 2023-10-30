# Setup Solr Cloud

After run docker compose up, you must be run

```shell
docker exec -it xxxxx sh

# you will be input user and password at here. 
# ex: username: solr, password: YourPassword
bin/solr auth enable -type basicAuth -prompt true -z zoo1:2181,zoo2:2181,zoo3:2181


# and update user and password at here 
export SOLR_AUTH_TYPE="basic" && export SOLR_AUTHENTICATION_OPTS="-Dbasicauth=solr:YourPassword" && bin/solr create -c solr_cloud -n data_driven_schema_configs -s 1 -rf 3

#important!: solr_cloud will be use to ping in HAProxy

#Generate Password on site https://clemente-biondo.github.io/ and update into ./haproxy/haproxy.cfg file and replace base64 endcode "Basic c29scjpPaG1pZGFzQDEyMw=="
```

```python

import findspark
findspark.init('/opt/spark')
from pyspark.sql import SparkSession
spark = SparkSession.builder\
    .enableHiveSupport()\
    .getOrCreate()

people = spark.createDataFrame([
    {"deptId": 1, "age": 40, "name": "Hyukjin Kwon", "gender": "M", "salary": 50},
    {"deptId": 1, "age": 50, "name": "Takuya Ueshin", "gender": "M", "salary": 100},
    {"deptId": 2, "age": 60, "name": "Xinrong Meng", "gender": "F", "salary": 150},
    {"deptId": 3, "age": 20, "name": "Haejoon Lee", "gender": "M", "salary": 200}
])

age_col = people.age

department = spark.createDataFrame([
    {"id": 1, "name": "PySpark"},
    {"id": 2, "name": "ML"},
    {"id": 3, "name": "Spark SQL"}
])

people.filter(people.age > 30).join(
    department, people.deptId == department.id).groupBy(
    department.name, "gender").agg({"salary": "avg", "age": "max"}).show()
```