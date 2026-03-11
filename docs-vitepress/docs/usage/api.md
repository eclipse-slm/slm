---
permalink: /docs/usage/api/
---

# API
All components of the Service Lifecycle Management have a REST API. More details about the individual APIs are described below.

## Authentication
All APIs are secured using token-based authentication via Keycloak. To get a access token from Keycloak, the request below can be used. It will return a JSON containing a filed `access_token`. This token must be used for authentication on the component APIs.
::: warning ATTENTION
`<<your-slm-host>>`, `<<your-username>>` and `<<your-password>>`must be replaced!
:::
```sh
curl --request POST \
  --url http://<<your-slm-host>>:7080/auth/realms/fabos/protocol/openid-connect/token \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data client_id=ui \
  --data grant_type=password \
  --data username=<<your-username>> \
  --data password=<<your-password>>
  ```

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
URL: `http://<<your-slm-host>>:{{ $slm.ports.platformManagement }}{{ $slm.basePaths.platformManagement }}`. \
API documentation is available via Swagger `http://<<your-slm-host>>:{{ $slm.ports.platformManagement }}{{ $slm.basePaths.platformManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Notification Service
URL: `http://<<your-slm-host>>:{{ $slm.ports.notificationService }}{{ $slm.basePaths.notificationService }}`. \
API documentation is available via Swagger `http://<<your-slm-host>>:{{ $slm.ports.notificationService }}{{ $slm.basePaths.notificationService }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Resource Management
URL: `http://<<your-slm-host>>:{{ $slm.ports.resourceManagement }}{{ $slm.basePaths.resourceManagement }}` \
API documentation is available via Swagger `http://<<your-slm-host>>:{{ $slm.ports.resourceManagement }}{{ $slm.basePaths.resourceManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Service Management
URL: `http://<<your-slm-host>>:{{ $slm.ports.serviceManagement }}{{ $slm.basePaths.serviceManagement }}` \
API documentation is available via Swagger `http://<<your-slm-host>>:{{ $slm.ports.serviceManagement }}{{ $slm.basePaths.serviceManagement }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Information Service
URL: `http://<<your-slm-host>>:{{ $slm.ports.informationService }}{{ $slm.basePaths.informationService }}` \
API documentation is available via Swagger `http://<<your-slm-host>>:{{ $slm.ports.informationService }}{{ $slm.basePaths.informationService }}/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.


## Postman
In order to simplify the use of the API, there is a public [Postman workspace](https://www.postman.com/fabos-ai/workspace/service-lifecycle-management). The requests can be viewed without a Postman account. For sending requests, an account and a locally installed [Postman desktop client](https://www.postman.com/downloads/) are required:

1) Login to your Postman account
2) Open the [Postman workspace](https://www.postman.com/fabos-ai/workspace/service-lifecycle-management) of the Service Lifecycle Management
3) Select `Collections` tab on the left and fork collection `Service Lifecycle Management` by right-clicking the collection and select `Create a fork`
   ![postman_fork_collection](/img/figures/api/postman_fork_collection.png)
4) Enter the required details and hit `Fork Collection`
   ![postman_fork_collection_details](/img/figures/api/postman_fork_collection_details.png)
5) Your selected Postman workspace will open and show the forked collection. Select `Environments` tab on the left and press `Import`. A dialog will show up and ask to `Upload Files`:
   ![postman_import_environment](/img/figures/api/postman_import_environment.png)
6) Select the file `postman-env.json` generated by the [Config Exporter](/docs/development/developers/#local-development). The file wil be parsed to an environment with the name schema `SLM - <<your-slm-hostname>>`. Press the `Import` button to finally import the environment.
7) To perform the request of the forked collection against your Service Lifecycle Management instance you need to select the imported environment in the upper right corner:
   ![postman_select_environment](/img/figures/api/postman_select_environment.png)
