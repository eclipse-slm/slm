resource "vsphere_virtual_machine" "slm-vm" {
  name             = "slm-${var.vsphere_user}"
  folder           = vsphere_folder.user_folder.path
  resource_pool_id = data.vsphere_compute_cluster.cluster.resource_pool_id
  datastore_id     = data.vsphere_datastore.datastore.id
  num_cpus         = 8
  memory           = 16384
  guest_id         = "ubuntu64Guest"
  firmware         = "efi"
  network_interface {
    network_id = data.vsphere_network.network.id
  }
  disk {
    label = "disk0"
    size  = 60
    thin_provisioned = false
  }
  clone {
    template_uuid = data.vsphere_content_library_item.item.id
    customize {
      linux_options {
        host_name = "slm-${split(var.username_delimiter,var.vsphere_user)[0]}"
        domain    = "slm.fabos.ai"
      }
      network_interface {}
    }
  }

  provisioner "file" {
    content   = templatefile(
      "scripts/run_slm.tftpl",
      {
        eclipse_slm_git_url       = var.git_slm_url
        eclipse_slm_branch        = var.git_slm_branch
        slm_hostname              = var.slm_hostname
        slm_ip                    = self.default_ip_address
        slm_build_docker_images   = var.slm_build_docker_images
        run_portainer             = var.run_portainer
        portainer_admin_password  = var.portainer_admin_password
      }
    )
    destination = "/tmp/run_slm.sh"

    connection {
      host     = self.default_ip_address
      type     = "ssh"
      user     = var.vm_username
      password = var.vm_password
    }
  }

  provisioner "remote-exec" {
    inline = [
      "sudo growpart /dev/sda 3",
      "sudo resize2fs /dev/sda3",
      "sudo lvextend -l +100%FREE /dev/ubuntu-vg/ubuntu-lv",
      "sudo resize2fs /dev/mapper/ubuntu--vg-ubuntu--lv",
      "sudo apt update && sudo apt install -y dos2unix",
      "sudo chmod +x /tmp/run_slm.sh",
      "dos2unix /tmp/run_slm.sh",
      "/tmp/run_slm.sh"
    ]

    connection {
      host        = self.default_ip_address
      type        = "ssh"
      user        = var.vm_username
      password    = var.vm_password
    }
  }
}

resource "vsphere_virtual_machine" "vms" {
  count = var.vm_count

  name             = "slm-${var.vsphere_user}-${count.index}"
  folder           = vsphere_folder.user_folder.path
  resource_pool_id = data.vsphere_compute_cluster.cluster.resource_pool_id
  datastore_id     = data.vsphere_datastore.datastore.id
  num_cpus         = var.num_cpus
  memory           = var.memory
  guest_id         = "ubuntu64Guest"
  firmware         = "efi"
  network_interface {
    network_id = data.vsphere_network.network.id
  }
  disk {
    label = "disk0"
    size  = var.disk_size
    thin_provisioned = false
  }
  clone {
    template_uuid = data.vsphere_content_library_item.item.id
    customize {
      linux_options {
        host_name = "slm-${split(var.username_delimiter,var.vsphere_user)[0]}-${count.index}"
        domain    = "slm.fabos.ai"
      }
      network_interface {}
    }
  }

  provisioner "file" {
    content   = templatefile(
      "scripts/add_vm_to_slm.tftpl",
      {
        slm_address   = vsphere_virtual_machine.slm-vm.default_ip_address
        slm_rm_port   = var.slm_rm_port
        slm_kc_port   = var.slm_kc_port
        vm_user       = var.vm_username
        vm_password   = var.vm_password
        vm_ip         = self.default_ip_address
        slm_user      = var.slm_username
        slm_password  = var.slm_password
      }
    )
    destination = "/tmp/add_vm_to_slm.sh"

    connection {
      host     = self.default_ip_address
      type     = "ssh"
      user     = var.vm_username
      password = var.vm_password
    }
  }

  provisioner "remote-exec" {
    inline = [
      "sudo apt update && sudo apt install -y dos2unix",
      "sudo apt remove -y unattended-upgrades",
      "sudo chmod +x /tmp/add_vm_to_slm.sh",
      "dos2unix /tmp/add_vm_to_slm.sh",
      "/tmp/add_vm_to_slm.sh"
    ]

    connection {
      host     = self.default_ip_address
      type     = "ssh"
      user     = var.vm_username
      password = var.vm_password
    }
  }
}

resource "vsphere_folder" "user_folder" {
  path          = "slm/${var.vsphere_user}"
  type          = "vm"
  datacenter_id = data.vsphere_datacenter.datacenter.id
}