# What it does

1. Creates one SLM-VM that is intended for the SLM installation
2. Clones SLM repository on SLM-VM and optionally builds SLM (mvn) and base components (docker-compose)
3. Starts the SLM (docker-compose up)
4. Creates SLM resources (virtual machines) via vmware vsphere instance
5. Adds VMs to SLM

# Requirements - local host
- terraform - https://developer.hashicorp.com/terraform/downloads

# How to

1. ...