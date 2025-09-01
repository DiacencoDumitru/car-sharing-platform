resource "google_cloud_run_v2_service" "eureka" {
  name     = "eureka-server"
  location = var.region

  template {
    containers {
      image = local.images["eureka-server"]
      ports { container_port = 8080 }
      env   = [for e in local.common_env : e if e.name != "SPRING_DATASOURCE_URL" && e.name != "DB_USERNAME" && e.name != "DB_PASSWORD"]
    }
    scaling {
      min_instance_count = 1
      max_instance_count = 2
    }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

resource "google_cloud_run_v2_service" "apigw" {
  name     = "api-gateway"
  location = var.region

  template {
    containers {
      image = local.images["api-gateway"]
      ports { container_port = 8080 }
      env   = local.common_env
    }
    scaling {
      min_instance_count = 1
      max_instance_count = 3
    }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

resource "google_cloud_run_v2_service_iam_member" "apigw_public" {
  project   = google_cloud_run_v2_service.apigw.project
  location  = google_cloud_run_v2_service.apigw.location
  name      = google_cloud_run_v2_service.apigw.name
  role      = "roles/run.invoker"
  member    = "allUsers"
}

resource "google_cloud_run_v2_service" "user" {
  name     = "user-service"
  location = var.region

  template {
    containers {
      image = local.images["user-service"]
      ports { container_port = 8080 }
      env   = local.common_env
    }
    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

resource "google_cloud_run_v2_service" "car" {
  name     = "car-service"
  location = var.region

  template {
    containers {
      image = local.images["car-service"]
      ports { container_port = 8080 }
      env   = local.common_env
    }
    scaling { max_instance_count = 3 }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

resource "google_cloud_run_v2_service" "booking" {
  name     = "booking-service"
  location = var.region

  template {
    containers {
      image = local.images["booking-service"]
      ports { container_port = 8080 }
      env   = local.common_env
    }
    scaling { max_instance_count = 3 }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

# Dispute service
resource "google_cloud_run_v2_service" "dispute" {
  name     = "dispute-service"
  location = var.region

  template {
    containers {
      image = local.images["dispute-service"]
      ports { container_port = 8080 }
      env   = local.common_env
    }
    scaling { max_instance_count = 3 }
  }
  ingress = "INGRESS_TRAFFIC_ALL"
  deletion_protection = false
}

output "api_gateway_url" { value = google_cloud_run_v2_service.apigw.uri }
output "eureka_url"      { value = google_cloud_run_v2_service.eureka.uri }