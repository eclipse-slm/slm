# What it does

1. Creates SLM resources (virtual machines) via Openstack
2. Optionally adds VMs to existing SLM

# Requirements - local host
- terraform - https://developer.hashicorp.com/terraform/downloads
- jq - https://stedolan.github.io/jq/download/ (if VMs shall be added to existing SLM)

# How to

1. Insert desired VM count and vsphere username to params.tfvars. If you want the VMs being added to a SLM instance set slm_hostname.

````terraform
vm_count      = 5
vsphere_user  = "<username>"
slm_hostname  = "insert-slm-ip-here"
````

2. Save your vsphere password to env variable

````shell
#Powershell
$env:TF_VAR_vsphere_password = 'pa$$w0rd'

#Shell
TF_VAR_vsphere_password=pa$$w0rd
````

3. If you want terraform to add the created VMs automatically to your SLM instance, make sure variable slm_hostname ist set in params.tfvars file:

````terraform
# params.tfvars
vm_count      = 5
vsphere_user  = "<username>"
slm_hostname  = "192.168.0.66" # example
````

4. Run init

````shell
terraform init
````

5. Run apply

````shell
terraform apply -auto-approve -var-file="params.tfvars"
````