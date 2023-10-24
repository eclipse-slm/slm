resource "openstack_compute_instance_v2" "slm-vm" {
  name            = "slm-${var.openstack_user}"
  image_id        = data.openstack_images_image_v2.slm-image.id
  flavor_id       = data.openstack_compute_flavor_v2.slm-flavor.id

  network {
    port = openstack_networking_port_v2.port_slm.id
  }

  key_pair = openstack_compute_keypair_v2.slm_dev_keypair.id

  provisioner "file" {
    content   = templatefile(
      "scripts/run_slm.tftpl",
      {
        eclipse_slm_git_url       = var.git_slm_url
        eclipse_slm_branch        = var.git_slm_branch
        slm_hostname              = var.slm_hostname
        slm_ip                    = openstack_networking_floatingip_v2.fip_slm.address
        slm_build_docker_images   = var.slm_build_docker_images
        run_portainer             = var.run_portainer
        portainer_admin_password  = var.portainer_admin_password
      }
    )
    destination = "/tmp/run_slm.sh"

    connection {
      host     = openstack_networking_floatingip_v2.fip_slm.address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

  provisioner "remote-exec" {
    inline = [
      "sudo reboot &"
    ]

    connection {
      host     = openstack_networking_floatingip_v2.fip_slm.address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

  provisioner "remote-exec" {
    inline = [
      "sudo apt remove -y unattended-upgrades && sudo apt update",
      "sudo useradd -m -s /usr/bin/bash -p $(openssl passwd -1 ${var.vm_password}) ${var.vm_username}",
      "sudo usermod -aG sudo ${var.vm_username}",
      "L='de' && sudo sed -i 's/XKBLAYOUT=\\\"\\w*\"/XKBLAYOUT=\\\"'$L'\\\"/g' /etc/default/keyboard",
      "sudo sed -i 's/PasswordAuthentication.*/PasswordAuthentication yes/g' /etc/ssh/sshd_config",
      "sudo systemctl restart sshd",
      "sudo apt install -y dos2unix",
      "sudo chmod +x /tmp/run_slm.sh",
      "dos2unix /tmp/run_slm.sh",
      "/tmp/run_slm.sh"
    ]

    connection {
      host     = openstack_networking_floatingip_v2.fip_slm.address
      type     = "ssh"
      user     = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }
}