#!/bin/bash
cd /runner/compose

startupSequenceInit=(
        "keycloak-init"
        "consul-init"
        "vault-init"
        "awx-init"
)

for service in "${startupSequenceInit[@]}"; do
        echo "Start $service..."
        docker compose up -d $service

        while [ $(docker inspect --format '{{json .State.Running}}' eclipse-slm-$service-1) == true ]; do
                echo "$service still running."
                sleep 5
        done
done

startupSequence=(
        "aas-registry"
        "prometheus"
)

for service in "${startupSequence[@]}"; do
        echo "Start $service..."
        docker compose up -d $service

        echo "Sleep for 10 seconds"
        sleep 10
done

echo "Start SLM DBs..."
docker compose up -d \
  resource-management-database \
  service-management-database \
  notification-service-database \
  catalog-database
sleep 10

echo "Start SLM Backend Services..."
docker compose up -d \
  resource-management-init \
  service-management-init \
  notification-service \
  catalog
sleep 10

echo "Start SLM Frontend..."
docker compose up -d ui

backendInitService=(
        "resource-management-init"
        "service-management-init"
)

for service in "${backendInitService[@]}"; do
        while [ $(docker inspect --format '{{json .State.Running}}' eclipse-slm-$service-1) == true ]; do
                echo "$service still running."
                sleep 5
        done
done

echo "SLM Startup finished."
