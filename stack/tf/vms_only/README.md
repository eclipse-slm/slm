# What it does

1. Creates SLM resources (virtual machines) via vmware vsphere instance
2. Optionally adds VMs to existing SLM

# Requirements - local host
- terraform - https://developer.hashicorp.com/terraform/downloads
- jq - https://stedolan.github.io/jq/download/

# How to

1. Insert desired VM count and vsphere username to params.tfvars. If you want the VMs being added to a SLM instance set slm_hostname.

````terraform
vm_count      = 5
vsphere_user  = "<username>"
slm_hostname  = "localhost"
````

2. Save your vsphere password to env variable

````shell
#Powershell
$env:TF_VAR_vsphere_password = 'pa$$w0rd'

#Shell
TF_VAR_vsphere_password=pa$$w0rd
````

3. If you want terraform to add the created VMs automatically to your SLM instance, make sure following part in resources.tf is not commented:

````terraform
# resources.tf
provisioner "local-exec" {
  command = "powershell -ExecutionPolicy Bypass .\\add_resource.ps1 ${var.slm_hostname} ${var.slm_username} ${var.slm_password} ${var.vm_username} ${var.vm_password} ${self.default_ip_address}"
}
````

4. Run init

````shell
terraform init
````

5. Run apply

````shell
terraform apply -auto-approve -var-file="params.tfvars"
````