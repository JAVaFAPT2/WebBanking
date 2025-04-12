FROM ubuntu:latest
LABEL authors="Admin"

ENTRYPOINT ["top", "-b"]

# Use OpenJDK base image
FROM openjdk:21-jdk-slim

# Set Kafka version and download URL
ARG KAFKA_VERSION=3.0.0
ARG SCALA_VERSION=2.13

# Set environment variables
ENV KAFKA_HOME=/opt/kafka
ENV PATH=$PATH:$KAFKA_HOME/bin

# Download and unpack Kafka
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && \
    tar -xvf kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz && \
    mv kafka_${SCALA_VERSION}-${KAFKA_VERSION} ${KAFKA_HOME} && \
    rm kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz

# Expose Kafka port
EXPOSE 9092

# Run Kafka in KRaft mode
CMD ["sh", "-c", "$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties"]
