# What it does

1. Creates one SLM-VM that is intended for the SLM installation
2. Installs maven, java, docker on SLM-VM
3. Optionally starts portainer (tf var: run_portainer) on SLM-VM
4. Clones SLM git repository (tf vars: eclipse_slm_branch, eclipse_slm_git_url)
5. Optionally build SLM and base components (tf var: slm_build_docker_images)
6. Starts the SLM (docker-compose up)
7. Creates SLM resources (virtual machines) via vmware vsphere instance
8. Adds VMs to SLM

# Requirements - local host
- terraform - https://developer.hashicorp.com/terraform/downloads

# How to

1. ...