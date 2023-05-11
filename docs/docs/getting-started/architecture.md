---
permalink: /docs/getting-started/architecture/
---

# Architecture

The architecture of the Service Lifecycle Management consists of serveral components:

![Service Lifecycle Management Architecture](/img/figures/architecture.png)

## Discovery Server
As Discovery Server [Consul](https://www.consul.io) from HashiCorp is used. It provides the following functionalities:
* List resources
* List services

## Credential Manager
As Credential Manager [Vault](https://www.vaultproject.io) from HashiCrop is used. It provides the following functionalities:
Provided functionalities:
* Management of SSH credentials
* PKI certificate authority
* Docker registry credentials
* Service credentials

## Configuration Manager
As Configuration manager [AWX](https://github.com/ansible/awx) from ReaHat is used. It provides the following functionalities:
Provided functionalities:
* Setup of Deployment Capabilites
* Deployment of services

## Identity and Access Management
For Identity and Access Management [Keycloak](https://www.keycloak.org) is used. It provides the following functionalities:
Provided functionalities:
* User Management
* Access Control
* Permissions and role Management

## Service Registry
The Service Registry is a custom implementation. It provides the following functionalities:
Provided functionalities:
* Catalog of service offerings
* Orchestration of service (un)deployments

The source code is expected to be made public by the end of 2022.

## Resource Registry
The Resource Registry is a custom implementation. It provides the following functionalities:
* Management of resources
* Management of Deployment Capabilites

The source code is expected to be made public by the end of 2022.

## UI
The UI is a custom implementation. It provides the following functionalities:
* User interface
* Web-based

The source code is expected to be made public by the end of 2022.