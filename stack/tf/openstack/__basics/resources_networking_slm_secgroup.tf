resource "openstack_networking_secgroup_v2" "slm_secgroup" {
  name        = "slm_secgroup"
  description = "allows all ports exposed by SLM"
}

resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_1" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.slm_secgroup.id
}