---
permalink: /docs/getting-started/architecture/
---

# Architecture

The architecture of the Service Lifecycle Management consists of serveral components:

![Architecture](/img/figures/architecture.svg)

## Components

### Discovery Server
As `Discovery Server` [Consul](https://www.consul.io) from HashiCorp is used. It provides the following functionalities:
* List resources
* List services
* List backend components
* Health monitoring of backend components

### Config Server
As `Config Server` [Consul](https://www.consul.io) KV value store is used. It provides the following functionalities:
* Central provisioning of runtime configuration
* Distribution of service configuration

### Credential Manager
As `Credential Manager` [Vault](https://www.vaultproject.io) from HashiCrop is used. It provides the following functionalities:
Provided functionalities:
* Management of SSH credentials
* PKI certificate authority
* Docker registry credentials
* Service credentials

### Configuration Manager
As `Configuration manager` [AWX](https://github.com/ansible/awx) from ReaHat is used. It provides the following functionalities:
Provided functionalities:
* Setup of Deployment Capabilites
* Deployment of services

### Identity and Access Management
For `Identity and Access Management` [Keycloak](https://www.keycloak.org) is used. It provides the following functionalities:
Provided functionalities:
* User Management
* Access Control
* Permissions and role Management

### Message Queue
As `Message Queue` [RabbitMQ](https://www.rabbitmq.com) is used. It provides the following functionalities:
* Asynchronous communication between backend components
* Reliable distribution of events and messages

### Reverse Proxy
As `Reverse Proxy` [Traefik](https://traefik.io/traefik/) is used. It provides the following functionalities:
* Central entry point for external HTTP(S) traffic
* Routing of requests to the corresponding backend components
* TLS termination for secured access to APIs and UI

### Service Management
The `Service Management` is a custom implementation. It provides the following functionalities:
Provided functionalities:
* Catalog of service offerings
* Orchestration of service (un)deployments

### Resource Management
The `Resource Management` is a custom implementation. It provides the following functionalities:
* Discovery of resources
* Management of resources
* Management of Deployment Capabilites
* Firmware update of resources

### Platform Management
The `Platform Management` is a custom implementation. It provides the following functionalities:
* Management of users
* Coordination of user provisioning
* Management of credential metadata and links

### Notification Service
The `Notification Service` is a custom implementation. It provides the following functionalities:
* Delivery of notifications for platform events
* Distribution of user-relevant status information

### Information Service
The `Information Service` is a custom implementation. It provides the following functionalities:
* Provision of aggregated platform information
* Read-oriented access to system information

### UI
The `UI` is a custom implementation. It provides the following functionalities:
* User interface
* Web-based

## HTTPS

In SLM, `Traefik` is used as the reverse proxy and TLS termination point for inbound HTTPS traffic. External clients connect to Traefik, and Traefik forwards 
requests to the internal components (e.g., UI, Resource Management, Service Management).

Certificate management is handled by `Vault`, which acts as the PKI for the SLM. Vault contains a dedicated SLM root certificate authority (Root CA). 
From this Root CA, an Intermediate CA is derived and used for issuing certificates to SLM backend components. Traefik obtains a short-lived leaf certificate 
from this PKI chain, which supports regular certificate rotation with reduced operational overhead.

If you need to expose SLM with your own public or enterprise certificate chain, a common approach is to place an additional proxy in front of Traefik. 
This proxy can handle TLS termination with your public or enterprise certificate, while Traefik continues to manage the internal certificates for SLM components.

If the SLM Root CA is added as a trusted CA on your operating system and in your browser, HTTPS communication with SLM is considered trusted on that client.

The SLM Root CA certificate can be downloaded from `http://<<your-slm-hostname>:{{ $slm.ports.vault }}/v1/pki_root_slm/cert/ca`.

