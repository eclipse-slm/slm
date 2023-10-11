## Get Image ID
data "openstack_networking_network_v2" "ext-net" {
  name = var.slm_dev_ext_network_name
}
