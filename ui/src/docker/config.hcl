consul {
  address = "consul:8500"
  #token = "pWNvsOmmEQ2UJx7Cg06WlJC6"
}

template {
  source =        "/consul-template/default.ctmpl"
  destination =   "/etc/nginx/conf.d/default.conf"
  exec {
    command = ["nginx", "-s", "reload"]
  }
}

exec {
  command = ["nginx", "-g", "daemon off;"]
}