terraform {
  required_version = ">= 0.14.0"
  required_providers {
    openstack = {
      source = "terraform-provider-openstack/openstack"
      version = "~> 1.51.1"
    }
  }
}

provider "openstack" {
  user_domain_name  = var.openstack_domain
  user_name         = var.openstack_user
  password          = var.openstack_password
  tenant_name       = var.openstack_project_name
  tenant_id         = var.openstack_project_id
  auth_url          = var.openstack_auth_url
  region            = var.openstack_region
}