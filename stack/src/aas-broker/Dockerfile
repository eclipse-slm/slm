FROM eclipse-mosquitto:2.0.22
COPY mosquitto.conf /mosquitto/config/

HEALTHCHECK --interval=5s --timeout=10s --start-period=1s --retries=3 \
 CMD mosquitto_sub -p 1884 -t 'topic' -C 1 -E -i probe -W 3