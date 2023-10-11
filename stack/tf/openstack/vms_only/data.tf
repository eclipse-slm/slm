data "openstack_images_image_v2" "image" {
  name        = var.openstack_image_name
  most_recent = true
}

data "openstack_compute_flavor_v2" "flavor" {
  name = var.openstack_flavor
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

