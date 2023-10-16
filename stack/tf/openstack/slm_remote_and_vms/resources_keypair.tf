resource "openstack_compute_keypair_v2" "slm_dev_keypair" {
  name = "slm-dev-keypair-${replace(split(var.username_delimiter,var.openstack_user)[0], ".", "-")}"
}