FROM prom/prometheus:v2.43.0

ENV CONSUL_URL=http://consul:8500
ENV CONSUL_TOKEN=0

COPY prometheus.yml /etc/prometheus/prometheus.yml
COPY startup.sh /etc/startup

ENTRYPOINT ["/etc/startup"]
CMD        [ "--config.file=/etc/prometheus/prometheus.yml", \
             "--storage.tsdb.path=/prometheus", \
             "--web.console.libraries=/usr/share/prometheus/console_libraries", \
             "--web.console.templates=/usr/share/prometheus/consoles" ]