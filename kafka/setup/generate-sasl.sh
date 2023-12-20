#!/usr/bin/env bash
 
set -eu

PASSWORD="${PASSWORD:-Ohmidas@123}"

SSL_FOLDER="sasl"
SECRETS_FOLDER="secrets"


rm -rf $SSL_FOLDER
mkdir $SSL_FOLDER
cd $SSL_FOLDER
 
echo
echo "Collect all file to $SECRETS_FOLDER"
mkdir $SECRETS_FOLDER
cd $SECRETS_FOLDER
cat << EOF > kafka_jaas.conf
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="$PASSWORD"
    user_admin="$PASSWORD"
    user_kafkaui="$PASSWORD"
    user_client="$PASSWORD";
};

KafkaClient {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    user_kafkaui="$PASSWORD"
    user_client="$PASSWORD";
};

Client {
       org.apache.zookeeper.server.auth.DigestLoginModule required
       username="kafka"
       password="$PASSWORD";
};
EOF

cat << EOF > zookeeper_jaas.conf
Server {
       org.apache.zookeeper.server.auth.DigestLoginModule required
       user_kafka="$PASSWORD";
};
EOF

cat << EOF > client.properties
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="client" password="$PASSWORD";
security.protocol=SASL_PLAINTEXT
EOF