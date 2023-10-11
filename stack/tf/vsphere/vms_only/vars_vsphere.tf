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