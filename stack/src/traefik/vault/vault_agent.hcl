vault {
  address = "http://vault:8200"
}

auto_auth {
  method "approle" {
    mount_path = "auth/approle"
    config = {
      role_id_file_path = "/vault/role_id"
      secret_id_file_path = "/vault/secret_id"
    }
  }
  sink "file" {
    config = {
      path = "/vault/token"
    }
  }
}

template_config {
  exit_on_retry_failure = false
  static_secret_render_interval = "10s"
}

template {
  source      = "/vault/cert.tpl"
  destination = "/certs/traefik-cert.pem"
  error_on_missing_key = true
  command     = "touch /certs/traefik_certs_dynamic_conf.yml"
}