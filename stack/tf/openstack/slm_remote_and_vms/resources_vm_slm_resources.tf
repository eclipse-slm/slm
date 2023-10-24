resource "openstack_compute_instance_v2" "vms" {
  timeouts {
    create = "1h"
  }
  depends_on = [
    openstack_compute_instance_v2.slm-vm
  ]
  count = var.vm_count

  name            = "slm-${var.openstack_user}-${count.index}"
  image_id        = data.openstack_images_image_v2.vm-image.id
  flavor_id       = data.openstack_compute_flavor_v2.vm-flavor.id

  network {
    port = openstack_networking_port_v2.port_vms[count.index].id
  }

  key_pair = openstack_compute_keypair_v2.slm_dev_keypair.id

  # Reboot:
  provisioner "remote-exec" {
    inline = [
      "(sleep 1 && sudo reboot) &"
    ]

    connection {
      host     = openstack_networking_floatingip_v2.fip_vms[count.index].address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

  # Add User, Change Keyboard Layout, Enable, SSH User Auth, apt Update/Upgrade/Remove/Install:
  provisioner "remote-exec" {
    inline = [
      "sudo useradd -m -s /usr/bin/bash -p $(openssl passwd -1 ${var.vm_password}) ${var.vm_username}",
      "sudo usermod -aG sudo ${var.vm_username}",
      "L='de' && sudo sed -i 's/XKBLAYOUT=\\\"\\w*\"/XKBLAYOUT=\\\"'$L'\\\"/g' /etc/default/keyboard",
      "sudo sed -i 's/PasswordAuthentication.*/PasswordAuthentication yes/g' /etc/ssh/sshd_config",
      "sudo systemctl restart sshd",
      "sudo apt purge -y libappstream4",
      "sudo export DEBIAN_FRONTEND=noninteractive && sudo apt update && sudo apt upgrade -y",
      "sudo apt remove -y unattended-upgrades",
      "sudo apt install -y dos2unix",
      "(sleep 1 && sudo reboot) &",
    ]

    connection {
      host     = openstack_networking_floatingip_v2.fip_vms[count.index].address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

  # Upload "Add VM to SLM" Script:
  provisioner "file" {
    content   = templatefile(
      "scripts/add_vm_to_slm.tftpl",
      {
        slm_address   = openstack_networking_floatingip_v2.fip_slm.address
        slm_rm_port   = var.slm_rm_port
        slm_kc_port   = var.slm_kc_port
        vm_user       = var.vm_username
        vm_password   = var.vm_password
        vm_ip         = openstack_networking_floatingip_v2.fip_vms[count.index].address
        slm_user      = var.slm_username
        slm_password  = var.slm_password
      }
    )
    destination = "/tmp/add_vm_to_slm.sh"

    connection {
      host     = openstack_networking_floatingip_v2.fip_vms[count.index].address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

  # Run "Add VM to SLM" Script:
  provisioner "remote-exec" {
    inline = [
      "sudo chmod +x /tmp/add_vm_to_slm.sh",
      "dos2unix /tmp/add_vm_to_slm.sh",
      "/tmp/add_vm_to_slm.sh"
    ]

    connection {
      host     = openstack_networking_floatingip_v2.fip_vms[count.index].address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }
}