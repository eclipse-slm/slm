variable "vm_count" {
  type = number
  default = 1
  nullable = false
}

variable "vm_username" {
  type = string
  default = "ansible"
}

variable "vm_password" {
  type = string
  default = "password"
}

variable "vm_image_name" {
  type = string
  default = "Ubuntu 22.04 64-bit"
  nullable = false
}

variable "vm_flavor" {
  type = string
  default = "m1.small"
  nullable = false
}