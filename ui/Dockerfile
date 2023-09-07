# Build stage
FROM node:lts as build-stage
WORKDIR /app
COPY package*.json ./
RUN apt update && apt install wget unzip
RUN wget https://releases.hashicorp.com/consul-template/0.32.0/consul-template_0.32.0_linux_amd64.zip && \
    unzip consul-template_0.32.0_linux_amd64.zip
RUN npm install --legacy-peer-deps --loglevel "error"
COPY . .
RUN npm run build --loglevel "error"

# Production stage
FROM nginx:stable as production-stage
EXPOSE 80

ENV NOTIFICATION_SERVICE_URL="http://notification-service:9001/" \
    RESOURCE_REGISTRY_URL="http://resource-management:9010/" \
    SERVICE_REGISTRY_URL="http://service-management:9020/" \
    KEYCLOAK_URL="http://keycloak:8080/auth" \
    KEYCLOAK_REALM="fabos" \
    KEYCLOAK_CLIENT_ID="ui" \
    AWX_URL="http://awxweb:80" \
    CATALOG_SERVICE_URL="http://catalog:10000/"

COPY --from=build-stage /app/consul-template /usr/bin
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY src/docker/config.hcl /consul-template/
COPY src/docker/default.ctmpl /consul-template/
COPY nginx_conf/nginx.conf /etc/nginx/nginx.conf

COPY src/docker/entrypoint.sh /app/entrypoint.sh

RUN rm /etc/nginx/conf.d/*
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]

CMD consul-template -config /consul-template/config.hcl & nginx -g "daemon off;"
