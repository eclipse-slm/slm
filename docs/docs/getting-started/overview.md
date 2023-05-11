---
permalink: /docs/getting-started/overview/
---

# Overview

## Motivation and Goals of the Service Lifecycle Management
Manufacturing companies invariably already have computing and storage resources but do not use all of them or not to the full extent. In addition, heterogeneous system landscapes, which are found in production environments, are difficult to manage. Often, complex and, in some cases even manual software deployment is required. Digital transformation and the Industrial Internet of Things are further increasing the number of devices to be managed and the proportion of software in production.

By centralizing and partially automating the management of such heterogeneous system landscapes, costs can be saved while improving the execution quality of software services at the same time. For this purpose, the properties and capabilities of the various systems in the production environment are described in an asset administration shell. This includes both static and dynamic information about the systems which is relevant for the provision of software. The requirements of software services are also described. This enables system properties and service requirements to be aligned so that the functionality and execution of software services can be guaranteed throughout their lifecycle.

A basic distinction can be made between software services and system resources, whereby services are executed on system resources. The properties of the resources are mapped via the asset administration shell, thus enabling both static and dynamic information to be described. Static information is available at a central location in an asset administration shell repository. Dynamic information is collected via a corresponding monitoring concept and made available in a manner compliant with the asset administration shell. The requirements of a software service are also described via an asset administration shell.

New software services are easy to order via a self-service portal. Once the order has been placed, FabOS Service Lifecycle Management automates the provisioning of the ordered service. In the process, the requirements of the ordered service are compared with the available features of the resources to make sure that the ordered services will function correctly. The present focus of provisioning is on container-based applications but this will be extended in the course of the project. The current status of the managed resources and provisioned services can be viewed in the self-service portal. 

## Integration into FabOS project
The Service Lifecycle Management forms the backbone for providing and executing software services in FabOS. With the asset administration shell, an open standard is used to describe system resources and software services. At the same time, it enables distributed execution and the utilization of available system resources in FabOS. It thus supports the use of the production services developed in FabOS and their execution in a production environment.