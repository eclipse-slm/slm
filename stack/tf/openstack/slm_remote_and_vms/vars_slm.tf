#SLM VARS:
variable "slm_image_name" {
  type = string
  default = "Ubuntu 22.04 64-bit"
}

variable "slm_flavor" {
  type = string
  default = "m1.xlarge"
}

variable "slm_hostname" {
  type = string
  default = "slm"
}

variable "slm_username" {
  type = string
  default = "fabos"
}

variable "slm_password" {
  type = string
  default = "password"
}

variable "slm_rm_port" {
  type = number
  default = 9010
}

variable "slm_kc_port" {
  type = number
  default = 7080
}

variable "slm_build_docker_images" {
  type = bool
  default = false
}

variable "run_portainer" {
  type = bool
  default = true
}

