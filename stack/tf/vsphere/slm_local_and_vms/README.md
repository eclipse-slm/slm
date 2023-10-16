# What it does

1. Builds all base (docker compose build) and SLM components (mvn package)
2. Starts the SLM (docker-compose up)
3. Creates SLM resources (virtual machines) via vmware vsphere instance
4. Adds VMs to SLM
5. Exports SLM config to <root-folder>/_config-generated with config-exporter

# Requirements - local host
- terraform - https://developer.hashicorp.com/terraform/downloads
- maven - https://maven.apache.org/download.cgi
- docker + docker-compose
- jq - https://stedolan.github.io/jq/download/

# How to

1. Insert desired VM count, that should be added to SLM, and vsphere username to params.tfvars
2. Save your vsphere password to env variable

````shell
#Powershell
$env:TF_VAR_vsphere_password = 'pa$$w0rd'

#Shell
TF_VAR_vsphere_password=pa$$w0rd
````

3. Run init

````shell
terraform init
````

3. Run apply

````shell
terraform apply -auto-approve -var-file="params.tfvars"
````