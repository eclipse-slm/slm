FROM ubuntu:latest
LABEL authors="michi"

RUN apt-get update; apt-get -y install curl

RUN curl https://dl.min.io/client/mc/release/linux-amd64/mc \
      --create-dirs \
      -o /app/mc

RUN chmod +x /app/mc


COPY startup.sh /app/startup.sh

WORKDIR /app

ENTRYPOINT ["/bin/bash", "-c", "/app/startup.sh"]