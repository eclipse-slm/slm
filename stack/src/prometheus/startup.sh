#!/bin/sh
# Prometheus refuses to use environment vars.

sed -i "s;%%CONSUL_URL%%;${CONSUL_URL};g" /etc/prometheus/prometheus.yml
sed -i "s/%%CONSUL_TOKEN%%/${CONSUL_TOKEN}/g" /etc/prometheus/prometheus.yml

cat /etc/prometheus/prometheus.yml

exec /bin/prometheus "$@"