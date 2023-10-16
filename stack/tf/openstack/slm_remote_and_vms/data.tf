data "openstack_images_image_v2" "vm-image" {
  name        = var.vm_image_name
  most_recent = true
}

data "openstack_images_image_v2" "slm-image" {
  name        = var.slm_image_name
  most_recent = true
}

data "openstack_compute_flavor_v2" "vm-flavor" {
  name = var.vm_flavor
}

data "openstack_compute_flavor_v2" "slm-flavor" {
  name = var.slm_flavor
}

data "openstack_networking_network_v2" "private" {
  name = var.openstack_network
}

data "openstack_networking_network_v2" "slm-dev" {
  name = var.slm_dev_network
}

data "openstack_networking_subnet_v2" "slm-dev" {
  name = var.slm_dev_subnetwork
}

data "openstack_networking_router_v2" "slm-dev" {
  name = var.slm_dev_router
}

data "openstack_networking_secgroup_v2" "slm-dev" {
  name = var.slm_dev_secgroup
}

