---
permalink: /docs/usage/profiler
---

# Profiler

## What are Profiler?

Profiler have been introduced in Release 1.3.0 of SLM. They are used to gather information about devices registered in 
SLM and publish them via AAS submodels registered in AAS of the respective device. Profiler can be added dynamically 
via the REST API of the [Resource Registry](/docs/usage/api/#resource-registry).

## Default Profiler
By default, SLM provides the following Profiler:

* [Ansible Facts](https://github.com/FabOS-AI/fabos-slm-pr-ansible-facts)
* [Webcams](https://github.com/FabOS-AI/fabos-slm-pr-webcams)

