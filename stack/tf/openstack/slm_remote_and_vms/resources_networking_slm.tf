resource "openstack_networking_port_v2" "port_slm" {
  name               = "port_slm"
  network_id         = data.openstack_networking_network_v2.slm-dev.id
  admin_state_up     = "true"

  fixed_ip {
    subnet_id  = data.openstack_networking_subnet_v2.slm-dev.id
  }
}

resource "openstack_networking_floatingip_v2" "fip_slm" {
  pool = data.openstack_networking_network_v2.private.name
}

resource "openstack_networking_floatingip_associate_v2" "fip_assoc_slm" {
  floating_ip = openstack_networking_floatingip_v2.fip_slm.address
  port_id     = openstack_networking_port_v2.port_slm.id
}

resource "openstack_networking_port_secgroup_associate_v2" "port_secgroup_assoc_slm" {
  port_id = openstack_networking_port_v2.port_slm.id
  security_group_ids = [
    data.openstack_networking_secgroup_v2.slm-dev.id
  ]
}