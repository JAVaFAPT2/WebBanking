#!/bin/bash
set -e

echo "Custom entrypoint script started as $(whoami)"

# Ensure KAFKA_LOG_DIRS is set and non-empty
if [ -z "${KAFKA_LOG_DIRS}" ]; then
  echo "Error: KAFKA_LOG_DIRS is not set. Exiting."
  exit 1
fi

echo "Attempting to chown/chmod ${KAFKA_LOG_DIRS}..."
mkdir -p "${KAFKA_LOG_DIRS}"
chown -R appuser:appuser "${KAFKA_LOG_DIRS}"
chmod -R u+rwx "${KAFKA_LOG_DIRS}"

echo "Permissions set for ${KAFKA_LOG_DIRS}."
echo "Executing original entrypoint /etc/confluent/docker/run as appuser using su..."

# Execute the original Confluent entrypoint as the appuser
# The quoting around $@ is important if the original entrypoint receives arguments
exec su -s /bin/bash -c '/etc/confluent/docker/run "$@"' appuser 