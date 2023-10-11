resource "openstack_networking_network_v2" "network_1" {
  name           = var.slm_dev_network_name
  admin_state_up = "true"
}

resource "openstack_networking_subnet_v2" "subnet_1" {
  name       = var.slm_dev_subnetwork_name
  network_id = openstack_networking_network_v2.network_1.id
  cidr       = var.slm_dev_subnetwork_cidr
  ip_version = 4
}

resource "openstack_networking_router_v2" "router_1" {
  name                = var.slm_dev_router_name
  admin_state_up      = true
  external_network_id = data.openstack_networking_network_v2.ext-net.id
}

resource "openstack_networking_router_interface_v2" "router_interface_1" {
  router_id = openstack_networking_router_v2.router_1.id
  subnet_id = openstack_networking_subnet_v2.subnet_1.id
}