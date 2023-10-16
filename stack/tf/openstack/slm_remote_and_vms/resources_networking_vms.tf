resource "openstack_networking_port_v2" "port_vms" {
  count = var.vm_count

  name               = "port_${count.index}"
  network_id         = data.openstack_networking_network_v2.slm-dev.id
  admin_state_up     = "true"

  fixed_ip {
    subnet_id  = data.openstack_networking_subnet_v2.slm-dev.id
  }
}

resource "openstack_networking_floatingip_v2" "fip_vms" {
  count = var.vm_count
  pool = data.openstack_networking_network_v2.private.name
}

resource "openstack_networking_floatingip_associate_v2" "fip_assoc_vms" {
  count = var.vm_count

  floating_ip = openstack_networking_floatingip_v2.fip_vms[count.index].address
  port_id     = openstack_networking_port_v2.port_vms[count.index].id
}