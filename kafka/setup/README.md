```shell
kafka-topics --bootstrap-server broker1:29092  --create --topic first-topic --partitions 1 --replication-factor 1 --command-config ~/client.properties
``` 