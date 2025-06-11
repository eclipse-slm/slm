# Build stage
FROM node:lts as build-stage
# Set working directory
WORKDIR /app
# Copy the package.json
COPY package*.json ./
# Install system dependencies
RUN apt update && apt install wget unzip
RUN wget https://releases.hashicorp.com/consul-template/0.32.0/consul-template_0.32.0_linux_amd64.zip && \
    unzip consul-template_0.32.0_linux_amd64.zip
# Install npm dependencies
RUN npm install --loglevel "error"
# Copy project files
COPY . .
# Build the application and remove node_modules
RUN npm run build --loglevel "error" && rm -rf node_modules

# Production Stage
FROM nginx:mainline-alpine-slim AS production-stage
# Define port 80 as exposed port
EXPOSE 80
# Define environment variables
ENV APP_VERSION="1.4.0-SNAPSHOT" \
    I18N_LOCALE="en" \
    I18N_LOCALE_FALLBACK="en" \
    NOTIFICATION_SERVICE_URL="http://notification-service:9001/" \
    RESOURCE_MANAGEMENT_URL="http://resource-management:9010/" \
    SERVICE_MANAGEMENT_URL="http://service-management:9020/" \
    CATALOG_SERVICE_URL="http://catalog:10000/" \
    KEYCLOAK_URL="http://keycloak:8080/auth" \
    KEYCLOAK_REALM="fabos" \
    KEYCLOAK_CLIENT_ID="ui" \
    AWX_URL="http://awxweb:80"

# Create a non-root user and group
RUN addgroup -g 1000 -S mygroup && \
    adduser -u 1000 -D -S -G mygroup myuser
# Install system dependencies
RUN apk add --no-cache curl
# Copy files from build stage to production stage
COPY --from=build-stage /app/consul-template /usr/bin
COPY --from=build-stage /app/dist /usr/share/nginx/html
# Copy the entrypoint script and make it executable
COPY src/docker/entrypoint.sh /app/entrypoint.sh
RUN rm /etc/nginx/conf.d/*
RUN chmod +x /app/entrypoint.sh
# Replace the default Nginx configuration file with a custom one and copy files for consul template (configures nginx)
COPY src/docker/nginx.conf /etc/nginx/nginx.conf
COPY src/docker/slm.conf /etc/nginx/conf.d/slm.conf
# Change the ownership of the /app, /var/cache/nginx and /tmp directories to the non-root user
RUN chown -R myuser:mygroup /app /var/cache/nginx /tmp /etc/nginx /usr/share/nginx/html
# Give the myuser/mygroup/other read/write/execute permissions to the /app, /var/cache/nginx and /tmp directories
RUN chmod -R 777 /app /var/cache/nginx /tmp /etc/nginx
# Switch to the non-root user
USER myuser
# Define the entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]