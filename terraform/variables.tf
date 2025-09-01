variable "aws_region" {
  description = "The AWS region to deploy resources in."
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "The name of the project, used for naming resources."
  type        = string
  default     = "dynamic-car-sharing"
}

variable "db_password" {
  description = "The password for the RDS database."
  type        = string
  sensitive   = true
}

variable "jwt_secret_key" {
  description = "The JWT secret key for the application."
  type        = string
  sensitive   = true
}

variable "image_tag" {
  description = "The Docker image tag (commit SHA) to deploy."
  type        = string
}