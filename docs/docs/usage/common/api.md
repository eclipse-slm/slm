---
permalink: /docs/usage/common/api/
---

# API
All components of the Service Lifecycle Management have a REST API. More details about the individual APIs are described below.

## Authentication
Protected API endpoints require authentication. Depending on the endpoint and component security configuration, one of the following authentication methods can be used:

- Bearer token via Keycloak
- API key via the `X-API-KEY` request header

### Authentication via bearer token (Keycloak)

To get an access token from Keycloak, the request below can be used. It will return a JSON containing a field `access_token`. This token can then be used for authenticated requests against the component APIs.
```sh
curl --request POST \
  --url http://<<your-slm-host>>:7080/auth/realms/fabos/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data client_id=ui \
  --data grant_type=password \
  --data username=<<your-username>> \
  --data password=<<your-password>>
  ```

### Authentication via API key

Some endpoints can also be called using API key authentication. In this case, the API key must be sent in the `X-API-KEY` request header.

This is especially useful for:

- automation scripts
- bootstrapping scenarios
- service-to-service communication without interactive user login

Example:

```sh
curl --request GET \
  --url https://<<your-slm-host>>/platform-management/users/example-user \
  --header 'X-API-KEY: <<your-api-key>>'
```

Whether API key authentication is supported depends on the respective endpoint and component security configuration.

## External Components

### AWX
Version: {{ $awx.version.full }} \
URL: `http://<<your-slm-host>>:{{ $awx.ports.web }}/api/v2` \
AWX is one of the open source upstream projects for [Red Hat Ansible Automation Platform](https://www.ansible.com/products/automation-platform). The commercial variant of RedHat is Ansible Tower. Therefore, information on how to use the AWX REST API can be found in the [Ansible Tower API Guide](https://docs.ansible.com/ansible-tower/latest/html/towerapi/index.html).

### BaSyx Discovery

Version: {{ $basyx.version.shell_registry }} \
URL: `http://<<your-slm-host>>:{{ $basyx.ports.discovery }}{{ $basyx.basePaths.discovery }}`

### BaSyx Shell Registry

Version: {{ $basyx.version.shell_registry }} \
URL: `http://<<your-slm-host>>:{{ $basyx.ports.shell_registry }}{{ $basyx.basePaths.shell_registry }}`

### BaSyx Submodel Registry

Version: {{ $basyx.version.sm_registry }} \
URL: `http://<<your-slm-host>>:{{ $basyx.ports.sm_registry }}{{ $basyx.basePaths.sm_registry }}`

### BaSyx Environment

Version: {{ $basyx.version.env }} \
URL `http://<<your-slm-host>>:{{ $basyx.ports.env }}{{ $basyx.basePaths.env }}`

### BaSyx AAS GUI

Version: {{ $basyx.version.gui }} \
URL: `http://<<your-slm-host>>{{ $basyx.basePaths.gui }}`.

### Consul
Version: {{ $consul.version.full }} \
URL: `http://<<your-slm-host>>:{{ $consul.ports.http }}/v1` \
Further information on how to use des Consul REST API can be found in the <a :href="'https://www.consul.io/api-docs/' + $consul.version.api" target="_blank">API docs of Consul</a>.

### Keycloak
Version: {{ $keycloak.version.full }} \
URL `https://<<your-slm-host>>:{{ $keycloak.ports.https }}/auth` \
For configuration Keycloak has a Admin REST API, which is described <a :href="'https://www.keycloak.org/docs-api/' + $keycloak.version.api + '/rest-api/' " target="_blank">here</a>.

### MinIO
Version: {{ $minio.version.full }} \
URL `https://<<your-slm-host>>:{{ $minio.ports.console }}` \

### Prometheus
Version: {{ $prometheus.version.full }} \
URL `https://<<your-slm-host>>:{{ $prometheus.ports.http }}` \

### RabbitMQ
Version: {{ $rabbitmq.version.full }} \
URL `https://<<your-slm-host>>:{{ $rabbitmq.ports.http }}` \

### Traefik
Version: {{ $traefik.version.full }} \
URL `https://<<your-slm-host>>:{{ $traefik.ports.dashboard }}`
### Vault
Version: {{ $vault.version.full }} \
The REST API of Vault is reachable under the following URL `http://<<your-slm-host>>:{{ $vault.ports.http }}/v1`. Further information on how to use des Consul REST API can be found in the <a :href="'https://www.vaultproject.io/api-docs/' + $vault.version.api" target="_blank">API docs of Vault</a>.

## SLM Components

### Catalog Service
URL: `http://<<your-slm-host>>:{{ $slm.ports.catalogService }}{{ $slm.basePaths.catalogService }}`

### Platform Management
URL: `http://<<your-slm-host>>/{{ $slm.basePaths.platformManagement }}`. \
API documentation is available via Swagger `https://<<your-slm-host>>/{{ $slm.basePaths.platformManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Notification Service
URL: `https://<<your-slm-host>>/{{ $slm.basePaths.notificationService }}` \
API documentation is available via Swagger `https://<<your-slm-host>>/{{ $slm.basePaths.notificationService }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Resource Management
URL: `https://<<your-slm-host>>/{{ $slm.basePaths.resourceManagement }}` \
API documentation is available via Swagger `https://<<your-slm-host>>/{{ $slm.basePaths.resourceManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Service Management
URL: `https://<<your-slm-host>>/{{ $slm.basePaths.serviceManagement }}` \
API documentation is available via Swagger `https://<<your-slm-host>>/{{ $slm.basePaths.serviceManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Information Service
URL: `https://<<your-slm-host>>{{ $slm.basePaths.informationService }}` \
API documentation is available via Swagger `https://<<your-slm-host>>/{{ $slm.basePaths.informationService }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.
