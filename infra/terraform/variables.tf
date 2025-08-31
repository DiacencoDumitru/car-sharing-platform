variable "aws_region" { type = string }
variable "ecr_registry" { type = string }
variable "image_tag"   { type = string }

variable "db_host"     { type = string, default = "your-rds-endpoint.rds.amazonaws.com" }
variable "db_user"     { type = string, default = "postgres" }
variable "db_password" { type = string, default = "password123" }
