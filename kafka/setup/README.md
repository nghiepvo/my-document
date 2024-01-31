```shell
kafka-topics --bootstrap-server broker1:29092  --create --topic first-topic --partitions 1 --replication-factor 1 --command-config ~/client.properties


kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name admin
kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name kafkaui
kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name client
``` 

https://jaehyeon.me/blog/2023-07-13-kafka-development-with-docker-part-10/


```shell
kafka-topics --bootstrap-server broker1:29092  --create --topic first-topic --partitions 1 --replication-factor 1 --command-config ~/client.properties

kafka-configs --bootstrap-server broker1:29092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name client --command-config ~/supper-admin.properties

kafka-configs --bootstrap-server broker1:29092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name producer --command-config ~/supper-admin.properties

kafka-configs --bootstrap-server broker1:29092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name consumer --command-config ~/supper-admin.properties

kafka-configs --bootstrap-server broker1:29092 --describe --entity-type users --command-config ~/supper-admin.properties

kafka-acls --bootstrap-server broker1:29092 --add --allow-principal User:client --operation All --group '*' --command-config ~/supper-admin.properties

kafka-acls --bootstrap-server broker1:29092 --list --command-config ~/client.properties

kafka-topics --bootstrap-server broker1:29092  --create --topic first-topic --partitions 1 --replication-factor 1 --command-config ~/client.properties
``` 

## Hello ksqlDB


```shell
docker compose -f docker-compose.sasl-ssl-scram.yml exec ksqldb-cli ksql http://ksqldb-server:8088
#We want to set the Ksql consumer to earliest so that we can see all messages that we will be producing. Run the following:

SET 'auto.offset.reset'='earliest';

CREATE STREAM users (
    ROWKEY INT KEY,
    USERNAME VARCHAR
) WITH (
    KAFKA_TOPIC='users',
    VALUE_FORMAT='JSON'
);

SHOW STREAMS;

INSERT INTO users (username) VALUES ('Mati');
INSERT INTO users (username) VALUES ('Michelle');
INSERT INTO users (username) VALUES ('John');

SELECT 'Hello, ' + USERNAME AS GREETING
FROM users
EMIT CHANGES;

docker compose -f docker-compose.sasl-ssl-scram.yml \
exec broker1 bash -c 'kafka-console-consumer --bootstrap-server broker1:29092 --from-beginning --topic users --consumer.config ~/client.properties'

```

