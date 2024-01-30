echo "Waiting for Kafka to come online..."

cub kafka-ready -b broker1:9092 1 20

echo "Generate client user..."

kafka-configs --bootstrap-server broker1:29092 --alter --add-config 'SCRAM-SHA-256=[iterations=8192,password=Ohmidas@123]' --entity-type users --entity-name client --command-config ~/supper-admin.properties

kafka-configs --bootstrap-server broker1:29092 --describe --entity-type users --command-config ~/supper-admin.properties

kafka-acls --bootstrap-server broker1:29092 --add --allow-principal User:client --operation All --group '*' --topic users --command-config ~/supper-admin.properties

kafka-acls --bootstrap-server broker1:29092 --list --command-config ~/supper-admin.properties

echo "Creating the users topic ..."

kafka-topics --bootstrap-server broker1:29092  --create --topic users --partitions 4 --replication-factor 1 --command-config ~/client.properties

sleep infinity