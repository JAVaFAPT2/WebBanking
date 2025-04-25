#!/bin/bash

echo "Creating Kafka topics..."

docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.transactions
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic transfer.debited
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic transaction.logged
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic transfer.failed
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.notifications
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic user-activity-topic
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.analytics
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.fraud.alerts
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.audit.logs
docker-compose exec kafka kafka-topics --create --if-not-exists --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --topic banking.dead-letter.queue

echo "Topics created successfully!"
