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

variable "openstack_network" {
  type = string
  default = "private"
  nullable = false
}

variable "slm_dev_network" {
  type = string
  default = "slm-dev-network"
  nullable = false
}

variable "slm_dev_subnetwork" {
  type = string
  default = "slm-dev-subnet"
  nullable = false
}

variable "slm_dev_router" {
  type = string
  default = "slm-dev-router"
  nullable = false
}

variable "slm_dev_keypair" {
  type = string
  default = "slm-dev-keypair"
  nullable = false
}

variable "slm_dev_secgroup" {
  type = string
  default = "slm_secgroup"
  nullable = false
}