# Build stage
FROM node:lts as build-stage
WORKDIR /app
COPY package*.json ./
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
    KEYCLOAK_CLIENT_ID="self-service-portal" \
    AWX_URL="http://awxweb:80" \
    CATALOG_SERVICE_URL="http://catalog:10000/"

COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx_conf/nginx.conf /etc/nginx/nginx.conf
COPY nginx_conf/default.conf.template /etc/nginx/conf.d/default.conf.template

COPY src/docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]

CMD ["nginx", "-g", "daemon off;"]
