resource "openstack_compute_instance_v2" "vms" {
  count = var.vm_count

  name            = "slm-${var.openstack_user}-${count.index}"
  image_id        = data.openstack_images_image_v2.image.id
  flavor_id       = data.openstack_compute_flavor_v2.flavor.id

  network {
    port = openstack_networking_port_v2.port[count.index].id
  }

  key_pair = openstack_compute_keypair_v2.slm_dev_keypair.id

  provisioner "remote-exec" {
    inline = [
      "sudo apt remove -y unattended-upgrades",
      "sudo useradd -m -s /usr/bin/bash -p $(openssl passwd -1 ${var.vm_password}) ${var.vm_username}",
      "sudo usermod -aG sudo ${var.vm_username}",
      "L='de' && sudo sed -i 's/XKBLAYOUT=\\\"\\w*\"/XKBLAYOUT=\\\"'$L'\\\"/g' /etc/default/keyboard",
      "sudo sed -i 's/PasswordAuthentication.*/PasswordAuthentication yes/g' /etc/ssh/sshd_config",
      "sudo systemctl restart sshd",
      "(sleep 3 && reboot) &"
    ]

    connection {
      host        = openstack_networking_floatingip_v2.fip[count.index].address
      type        = "ssh"
      user        = "ubuntu"
      private_key = openstack_compute_keypair_v2.slm_dev_keypair.private_key
    }
  }

#  requires jq -> https://stedolan.github.io/jq/download/
  provisioner "local-exec" {
    command = "powershell -ExecutionPolicy Bypass ..\\..\\_common\\add_vm_to_slm.ps1 ${var.slm_hostname} ${var.slm_username} ${var.slm_password} ${var.vm_username} ${var.vm_password} ${openstack_networking_floatingip_v2.fip[count.index].address}"
  }
}