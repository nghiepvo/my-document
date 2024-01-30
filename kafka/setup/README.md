```shell
kafka-topics --bootstrap-server broker1:29092  --create --topic first-topic --partitions 1 --replication-factor 1 --command-config ~/client.properties


kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name admin
kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name kafkaui
kafka-configs --zookeeper zookeeper:2181 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name client
``` 

https://jaehyeon.me/blog/2023-07-13-kafka-development-with-docker-part-10/