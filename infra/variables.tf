variable "aws_region" {
  type    = string
  default = "eu-central-1"
}

variable "project_name" {
  type    = string
  default = "dynamiccarsharing"
}

variable "db_name" {
  type    = string
  default = "dynamic_car_sharing_db"
}

variable "db_username" {
  type    = string
  default = "postgres"
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "car_service_desired_count" {
  type    = number
  default = 2
}
