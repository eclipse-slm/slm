#!/bin/sh
# Informational message
echo "========================================="
echo "Starting NGINX with base path: /"
echo "========================================="
echo

echo "nginx configuration:"
echo "----------------------------------------------------------------"
echo "etc/nginx/nginx.conf:"
echo "_____________________"
cat /etc/nginx/nginx.conf
echo "_____________________"
echo "/etc/nginx/conf.d/slm.conf:"
echo "_____________________"
cat /etc/nginx/conf.d/slm.conf
echo "_____________________"
echo "----------------------------------------------------------------"
echo

echo "Environment variables:"
echo "-------------------------------------------------------------------------------------------------------------------------"
printf "%-38s %s\n" "APP_VERSION:" "$APP_VERSION"
printf "%-38s %s\n" "I18N_LOCALE:" "$I18N_LOCALE"
printf "%-38s %s\n" "I18N_LOCALE_FALLBACK:" "$I18N_LOCALE_FALLBACK"
printf "%-38s %s\n" "NOTIFICATION_SERVICE_URL:" "$NOTIFICATION_SERVICE_URL"
printf "%-38s %s\n" "RESOURCE_MANAGEMENT_URL:" "$RESOURCE_MANAGEMENT_URL"
printf "%-38s %s\n" "SERVICE_MANAGEMENT_URL:" "$SERVICE_MANAGEMENT_URL"
printf "%-38s %s\n" "CATALOG_SERVICE_URL:" "$CATALOG_SERVICE_URL"
printf "%-38s %s\n" "KEYCLOAK_URL:" "$KEYCLOAK_URL"
printf "%-38s %s\n" "KEYCLOAK_REALM:" "$KEYCLOAK_REALM"
printf "%-38s %s\n" "KEYCLOAK_CLIENT_ID:" "$KEYCLOAK_CLIENT_ID"
printf "%-38s %s\n" "AWX_URL:" "$AWX_URL"
printf "%-38s %s\n" "BASYX_AAS_GUI_URL:" "$BASYX_AAS_GUI_URL"

# Replace the placeholders in all relevant files (.js, .html, .css)
find /usr/share/nginx/html -type f \( -name '*.js' -o -name '*.html' -o -name '*.css' \) -exec sed -i \
    -e "s|/__ENV_APP_VERSION__PLACEHOLDER__/|$APP_VERSION|g" \
    -e "s|/__ENV_I18N_LOCALE__PLACEHOLDER__/|$I18N_LOCALE|g" \
    -e "s|/__ENV_I18N_LOCALE_FALLBACK__PLACEHOLDER__/|$I18N_LOCALE_FALLBACK|g" \
    -e "s|/__ENV_NOTIFICATION_SERVICE_URL__PLACEHOLDER__/|$NOTIFICATION_SERVICE_URL|g" \
    -e "s|/__ENV_RESOURCE_MANAGEMENT_URL__PLACEHOLDER__/|$RESOURCE_MANAGEMENT_URL|g" \
    -e "s|/__ENV_SERVICE_MANAGEMENT_URL__PLACEHOLDER__/|$SERVICE_MANAGEMENT_URL|g" \
    -e "s|/__ENV_CATALOG_SERVICE_URL__PLACEHOLDER__/|$CATALOG_SERVICE_URL|g" \
    -e "s|/__ENV_KEYCLOAK_URL__PLACEHOLDER__/|$KEYCLOAK_URL|g" \
    -e "s|/__ENV_KEYCLOAK_REALM__PLACEHOLDER__/|$KEYCLOAK_REALM|g" \
    -e "s|/__ENV_KEYCLOAK_CLIENT_ID__PLACEHOLDER__/|$KEYCLOAK_CLIENT_ID|g" \
    -e "s|/__ENV_AWX_URL__PLACEHOLDER__/|$AWX_URL|g" \
    -e "s|/__ENV_BASYX_AAS_GUI_URL__PLACEHOLDER__/|$BASYX_AAS_GUI_URL|g" \
    {} \;

# Wait until Keycloak is running
echo "Keycloak availability test URL: $KEYCLOAK_URL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration"
until curl -m 5 -s -k --location --request GET "$KEYCLOAK_URL/realms/$KEYCLOAK_REALM/.well-known/openid-configuration" > /dev/null; do
  echo "Keycloak is unavailable -> sleeping"
  sleep 1
done

# Wait until Notification Service is running
until curl -m 5 -s -k --location --request GET "$NOTIFICATION_SERVICE_URL/v3/api-docs" > /dev/null; do
  echo "Notification Service is unavailable -> sleeping"
  sleep 1
done

# Wait until Resource Management is running
until curl -m 5 -s -k --location --request GET "$RESOURCE_MANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Resource Management is unavailable -> sleeping"
  sleep 1
done

# Wait until Service Management is running
until curl -m 5 -s -k --location --request GET "$SERVICE_MANAGEMENT_URL/v3/api-docs" > /dev/null; do
  echo "Service Management is unavailable -> sleeping"
  sleep 1
done

exec nginx -g "daemon off;"
