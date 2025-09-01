variable "project_id" { type = string }
variable "region"     { type = string  default = "europe-central2" }
variable "repo"       { type = string  default = "microservices" }

variable "image_tag"  { type = string }

variable "ar_base" {
  type    = string
  default = ""
}

locals {
  ar_base = "${var.region}-docker.pkg.dev/${var.project_id}/${var.repo}"

  images = {
    "eureka-server" = "${local.ar_base}/eureka-server:${var.image_tag}"
    "api-gateway"   = "${local.ar_base}/api-gateway:${var.image_tag}"
    "user-service"  = "${local.ar_base}/user-service:${var.image_tag}"
    "car-service"   = "${local.ar_base}/car-service:${var.image_tag}"
    "booking-service" = "${local.ar_base}/booking-service:${var.image_tag}"
    "dispute-service" = "${local.ar_base}/dispute-service:${var.image_tag}"
  }

  common_env = [
    { name = "EUREKA_URL", value = "${google_cloud_run_v2_service.eureka.uri}/eureka/" },
    { name = "ZIPKIN_URL",          value = var.zipkin_url },
    { name = "SPRING_DATASOURCE_URL", value = var.db_url },
    { name = "DB_USERNAME",         value = var.db_user },
    { name = "DB_PASSWORD",         value = var.db_password },
    { name = "JWT_SECRET",          value = var.jwt_secret }
  ]
}

variable "db_url"      { type = string }
variable "db_user"     { type = string }
variable "db_password" { type = string }
variable "zipkin_url"  { type = string default = "http://zipkin:9411/api/v2/spans" }
variable "jwt_secret"  { type = string }
