---
permalink: /docs/usage/capabilities
---

# Capabilities

## Deployment Capabilities
Deployment Capabilities are used to deploy a service on a resource. They can be added dynamically via the REST API of the [Resource Registry](/docs/usage/api/#resource-registry). To simplify the use of the API the requests related to capabilites are available in the [public Postman workspace](https://www.postman.com/fabos-ai/workspace/service-lifecycle-management/folder/22732344-bc9fe87a-30cc-41aa-a3d9-4d1d39a1f4e4). The initial setup of Postman is described [here](/docs/usage/api/#postman).

### Import officially supported Deployment Capabilities
After installation, a new Service Lifecycle Management instance has no deployment capabilities. By default, the compose stack of the Service Lifecycle Management includes a service `resource-registry-init`. This service imports the officially supported capabilities during the first startup:
* [Docker](https://github.com/eclipse-slm/slm-dc-docker)
* [Docker Swarm](https://github.com/eclipse-slm/slm-dc-docker-swarm)
* [K3S](https://github.com/eclipse-slm/slm-dc-k3s)
* [K8S](https://github.com/eclipse-slm/slm-dc-k8s)

### Add a new Deployment Capability
Additional deployment capabilites can be imported using the endpoint `POST /resources/capabilites` of the Resource Registry. A request at the example of the [Docker Deployment Capability](https://github.com/FabOS-AI/fabos-slm-dc-docker) looks like this:
::: warning ATTENTION
`<<your-slm-host>>` and [`<<your-keycloak-token>>`](http://localhost:8081/docs/usage/api/#authentication) must be replaced!
:::
```sh
curl -X 'POST' \
  'http://<<your-slm-host>>:9010/resources/capabilities' \
  -H 'accept: application/json' \
  -H 'Realm: fabos' \
  -H 'Authorization: Bearer <<your-keycloak-token>>' \
  -H 'Content-Type: application/json' \
  -d '{
    "uuid": "08c5b8de-5d4a-4116-a73f-1d1f616c7c70",
    "capabilityClass": "DeploymentCapability",
    "name": "Docker",
    "logo": "mdi-docker",
    "type": [
      "setup",
      "deploy"
    ],
    "actions": {
      "install": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "install",
        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",
        "awxBranch": "main",
        "playbook": "install.yml"
      },
      "uninstall": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "uninstall",
        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",
        "awxBranch": "main",
        "playbook": "uninstall.yml"
      },
      "undeploy": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "undeploy",
        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",
        "awxBranch": "main",
        "playbook": "undeploy.yml"
      },
      "deploy": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "deploy",
        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",
        "awxBranch": "main",
        "playbook": "deploy.yml"
      },
    },
    "supportedDeploymentTypes": [
      "DOCKER_CONTAINER",
      "DOCKER_COMPOSE"
    ],
    "clusterMemberTypes": []
  }''
```

If a Deployment Capability is located in a private git repository, username and password / access token must be set for each action:
```json
...
"actions": {
    ...
    "install": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "install",
        "awxRepo": "https://my-private-git-repo/slm-dc-private",
        "awxBranch": "main",
        "playbook": "install.yml",
        "username": "myUser",
        "password": "myPassword"
      },
      ...
}
...
```

### Use deployment capability to deploy service offerings
To allow a deployment capability to deploy a service offering with a particular deployment type, that deployment type must be defined in the `supportedDeploymentTypes` property of the deployment capability. Currently, service offerings can have the following deployment types:
* DOCKER_CONTAINER 
* DOCKER_COMPOSE
* KUBERNETES