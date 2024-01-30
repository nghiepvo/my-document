echo "Waiting for Kafka to come online..."

cub kafka-ready -b broker1:9092 1 20

# create the users topic
kafka-topics \
  --bootstrap-server broker1:9092 \
  --topic users \
  --replication-factor 1 \
  --partitions 4 \
  --create

sleep infinity