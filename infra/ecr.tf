resource "aws_ecr_repository" "car_service" {
  name                 = "${var.project_name}/car-service"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}
