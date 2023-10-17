##### GITHUB
variable "git_slm_url" {
  type = string
  default = "https://github.com/eclipse-slm/slm /tmp/eclipse-slm"
}

variable "git_slm_branch" {
  type = string
  default = "develop"
}


##### VSPHERE

variable "vsphere_user" {
  type = string
  nullable = false
}

variable "username_delimiter" {
  type = string
  default = "@"
  nullable = false
}

variable "vsphere_password" {
  type = string
  nullable = false
  sensitive = true
}

variable "vsphere_server" {
  type = string
  nullable = false
  default = "ansiblevcenter.main.ansible.fortknox.local"
}


##### VM

variable "vm_count" {
  type = number
  default = 1
  nullable = false
}

variable "vm_template_name" {
  type = string
  default = "ubuntu-22-04"
  nullable = false
}

variable "vm_username" {
  type = string
  default = "ansible"
}

variable "vm_password" {
  type = string
  default = "password"
}

variable "num_cpus" {
  type = number
  default = 2
  nullable = false
}

variable "memory" {
  type = number
  default = 2048
  nullable = false
}

variable "disk_size" {
  type = number
  default = 40
  nullable = false
}


#SLM VARS:

variable "slm_hostname" {
  type = string
  default = "slm"
}

variable "slm_username" {
  type = string
  default = "fabos"
}

variable "slm_password" {
  type = string
  default = "password"
}

variable "slm_rm_port" {
  type = number
  default = 9010
}

variable "slm_kc_port" {
  type = number
  default = 7080
}

variable "slm_build_docker_images" {
  type = bool
  default = false
}

variable "run_portainer" {
  type = bool
  default = true
}

variable "portainer_admin_password" {
  type = string
  default = "passwordpassword"
}