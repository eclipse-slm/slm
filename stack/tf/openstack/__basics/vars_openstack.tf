variable "slm_dev_network_name" {
  type = string
  nullable = false
  default = "slm-dev-network"
}

variable "slm_dev_subnetwork_name" {
  type = string
  nullable = false
  default = "slm-dev-subnet"
}

variable "slm_dev_subnetwork_cidr" {
  type = string
  nullable = false
  default = "192.168.199.0/24"
}

variable "slm_dev_router_name" {
  type = string
  nullable = false
  default = "slm-dev-router"
}

variable "slm_dev_ext_network_name" {
  type = string
  nullable = false
  default = "private"
}

variable "slm_dev_keypair" {
  type = string
  nullable = false
  default = "slm-dev-keypair"
}

variable "openstack_user" {
  type = string
  nullable = false
}

variable "username_delimiter" {
  type = string
  default = "@"
  nullable = false
}

variable "openstack_password" {
  type = string
  nullable = false
  sensitive = true
}

variable "openstack_auth_url" {
  type = string
  nullable = false
  default = "http://keystone-api.fec-ipa-001.gecgo.net/v3"
}

variable "openstack_domain" {
  type = string
  nullable = false
  default = "slm-dev"
}

variable "openstack_project_name" {
  type = string
  nullable = false
  default = "admin"
}

variable "openstack_project_id" {
  type = string
  nullable = false
  default = "72424719a3c543e2b8f0f77fe5fc8ea3"
}

variable "openstack_region" {
  type = string
  nullable = false
  default = "RegionOne"
}