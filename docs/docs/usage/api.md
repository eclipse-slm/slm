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

## Components

### AWX
Version: {{ $var.awx.version.full }} \
The REST API of AWX is reachable under the following URL `http://<<your-slm-host>>:80/api/v2`. AWX is one of the open source upstream projects for [Red Hat Ansible Automation Platform](https://www.ansible.com/products/automation-platform). The commercial variant of RedHat is Ansible Tower. Therefore, information on how to use the AWX REST API can be found in the [Ansible Tower API Guide](https://docs.ansible.com/ansible-tower/latest/html/towerapi/index.html).

### Consul
Version: {{ $var.consul.version.full }} \
The REST API of Consul is reachable under the following URL `http://<<your-slm-host>>:8500/v1`. Further information on how to use des Consul REST API can be found in the <a :href="'https://www.consul.io/api-docs/' + $var.consul.version.api" target="_blank">API docs of Consul</a>.

### Keycloak
Version: {{ $var.keycloak.version.full }} \
The REST API of Keycloak is reachable under the following URL `http://<<your-slm-host>>:7080/auth`. For configuration Keycloak has a Admin REST API, which is described <a :href="'https://www.keycloak.org/docs-api/' + $var.keycloak.version.api + '/rest-api/' " target="_blank">here</a>.

### BaSyx AAS Registry

Version: {{ $var.basyx.version.registry }} \
The REST API of BaSyx AAS Registry is reachable under the following URL `http://<<your-slm-host>>:4000/registry`. API documentation is available on SwaggerHub: <https://app.swaggerhub.com/apis/BaSyx/BaSyx_Registry_API/v1#/>

### BaSyx AAS Server

Version: {{ $var.basyx.version.server }} \
The REST API of BaSyx AAS Server is reachable under the following URL `http://<<your-slm-host>>:4001/aasServer`. API documentation is available on SwaggerHub: <https://app.swaggerhub.com/apis/BaSyx/basyx_asset_administration_shell_repository_http_rest_api/v1>

### BaSyx AAS GUI

Version: {{ $var.basyx.version.gui }} \
The web interface of the BaSyx/Fraunhofer [AAS GUI](https://github.com/eclipse-basyx/basyx-applications/tree/main/aas-gui) is reachable under `http://<<your-slm-host>>:3000`.

### Notification Service
The REST API of the Service Registry is reachable under the following URL `http://<<your-slm-host>>:9001`. API documentation is available via Swagger `http://<<your-slm-host>>:9001/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Resource Registry
The REST API of the Service Registry is reachable under the following URL `http://<<your-slm-host>>:9010`. API documentation is available via Swagger `http://<<your-slm-host>>:9010/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Service Registry
The REST API of the Service Registry is reachable under the following URL `http://<<your-slm-host>>:9020`. API documentation is available via Swagger `http://<<your-slm-host>>:9020/swagger-ui/index.html`. When requests are made via the Swagger UI, an Authorization is required via the "Authorize" button. If spring_oauth is used, it will redirect to the Keycloak login page.

### Vault
Version: {{ $var.vault.version.full }} \
The REST API of Keycloak is reachable under the following URL `http://<<your-slm-host>>:8200/v1`. Further information on how to use des Consul REST API can be found in the <a :href="'https://www.vaultproject.io/api-docs/' + $var.vault.version.api" target="_blank">API docs of Vault</a>.

## Postman
In order to simplify the use of the API, there is a public [Postman workspace](https://www.postman.com/fabos-ai/workspace/service-lifecycle-management). The requests can be viewed without a Postman account. For sending requests, an account and a locally installed [Postman desktop client](https://www.postman.com/downloads/) are required:

1) Login to your Postman account
2) Open the [Postman workspace](https://www.postman.com/fabos-ai/workspace/service-lifecycle-management) of the Service Lifecycle Management
3) Select `Collections` tab on the left and fork collection `Service Lifecycle Management` by right-clicking the collection and select `Create a fork`
   <img :src="$withBase('/img/figures/api/postman_fork_collection.png')">
4) Enter the required details and hit `Fork Collection`
   <img :src="$withBase('/img/figures/api/postman_fork_collection_details.png')">
5) Your selected Postman workspace will open and show the forked collection. Select `Environments` tab on the left and press `Import`. A dialog will show up and ask to `Upload Files`:
   <img :src="$withBase('/img/figures/api/postman_import_environment.png')">
6) Select the file `postman-env.json` generated by the [Config Exporter](/docs/development/developers/#local-development). The file wil be parsed to an environment with the name schema `SLM - <<your-slm-hostname>>`. Press the `Import` button to finally import the environment.
7) To perform the request of the forked collection against your Service Lifecycle Management instance you need to select the imported environment in the upper right corner:
   <img :src="$withBase('/img/figures/api/postman_select_environment.png')">
