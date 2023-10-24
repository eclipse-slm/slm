data "vsphere_datacenter" "datacenter" {
  name = "Ansible"
}

data "vsphere_datastore" "datastore" {
  name          = "Datastore01"
  datacenter_id = data.vsphere_datacenter.datacenter.id
}

data "vsphere_compute_cluster" "cluster" {
  name          = "Ansible"
  datacenter_id = data.vsphere_datacenter.datacenter.id
}

data "vsphere_network" "network" {
  name          = "VM-Network"
  datacenter_id = data.vsphere_datacenter.datacenter.id
}

data "vsphere_content_library" "library" {
  name = "templates"
}

data "vsphere_content_library_item" "item" {
  name       = var.vm_template_name
  type       = "ovf"
  library_id = data.vsphere_content_library.library.id
}