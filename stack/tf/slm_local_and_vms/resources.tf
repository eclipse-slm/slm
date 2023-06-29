resource "null_resource" "local-slm" {
  triggers = {
    always_run = timestamp()
  }

  // requires maven: https://maven.apache.org/download.cgi and java jdk in version set in pom.xml
  provisioner "local-exec" {
    working_dir = "../../.."
    command = "mvn -DskipTests=true -T 8 -f pom.xml clean package"
  }

  provisioner "local-exec" {
    working_dir = "../../compose"
    command     = <<-EOT
      docker compose -f base.awx.yml -f base.consul.yml -f base.keycloak.yml -f base.vault.yml -f slm.yml -f catalog.yml -f config-exporter.yml -f build.yml build awx-fixed-files
    EOT
  }

  provisioner "local-exec" {
    working_dir = "../../compose"
    command     = <<-EOT
      docker compose -f base.awx.yml -f base.consul.yml -f base.keycloak.yml -f base.vault.yml -f slm.yml -f catalog.yml -f config-exporter.yml -f build.yml build awx-web awx-web-no-jwt awx-task awx-postgres awx-init common-database-mariadb consul consul-init keycloak keycloak-init vault vault-init config-exporter
    EOT
  }

  provisioner "local-exec" {
    working_dir = "../../compose"
    command = "docker-compose up -d"
  }

  provisioner "local-exec" {
    working_dir = "../../compose"
    command = "docker-compose down -v --remove-orphans"
    when = destroy
  }
}

resource "vsphere_folder" "user_folder" {
  path          = "slm/${var.vsphere_user}"
  type          = "vm"
  datacenter_id = data.vsphere_datacenter.datacenter.id
}

resource "vsphere_virtual_machine" "vms" {
  depends_on = [null_resource.local-slm]

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

  provisioner "remote-exec" {
    inline = [
      "sudo apt remove -y unattended-upgrades",
    ]

    connection {
      host     = self.default_ip_address
      type     = "ssh"
      user     = var.vm_username
      password = var.vm_password
    }
  }

#  requires jq -> https://stedolan.github.io/jq/download/
  provisioner "local-exec" {
    command = "powershell -ExecutionPolicy Bypass .\\add_resource.ps1 ${var.slm_hostname} ${var.slm_username} ${var.slm_password} ${var.vm_username} ${var.vm_password} ${self.default_ip_address}"
  }
}

resource "null_resource" "config-exporter" {
  depends_on = [vsphere_virtual_machine.vms]

  provisioner "local-exec" {
    working_dir = "../../compose"
    command     = "docker compose up --force-recreate config-exporter"
  }
}
