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

echo "Start SLM..."
docker compose up -d
