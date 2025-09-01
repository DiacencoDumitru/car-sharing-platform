resource "aws_ecr_repository" "app_repos" {
  for_each = toset([
    "eureka-server",
    "api-gateway",
    "user-service",
    "car-service",
    "booking-service",
    "dispute-service",
  ])
  name = each.key
}

resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"
}

data "aws_iam_policy_document" "ecs_task_execution_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "${var.project_name}-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_role_policy.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

locals {
  ecr_base_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
}
data "aws_caller_identity" "current" {}


resource "aws_ecs_task_definition" "eureka" {
  family                   = "eureka-server"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  container_definitions = jsonencode([
    {
      name      = "eureka-server"
      image     = "${local.ecr_base_url}/eureka-server:${var.image_tag}"
      essential = true
      portMappings = [{ containerPort = 8761 }]
    }
  ])
}

resource "aws_ecs_service" "eureka" {
  name            = "eureka-server-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.eureka.arn
  desired_count   = 1
  launch_type     = "FARGATE"
  network_configuration {
    subnets         = aws_subnet.public[*].id
    security_groups = [aws_security_group.ecs_sg.id]
  }
  service_registries {
    registry_arn = aws_service_discovery_service.eureka.arn
  }
}

resource "aws_ecs_task_definition" "api_gateway" {
  family                   = "api-gateway"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  container_definitions = jsonencode([
    {
      name      = "api-gateway"
      image     = "${local.ecr_base_url}/api-gateway:${var.image_tag}"
      essential = true
      portMappings = [{ containerPort = 8085 }]
      environment = [
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka-server.local:8761/eureka/" },
        { name = "APPLICATION_SECURITY_JWT_SECRET-KEY", value = var.jwt_secret_key }
      ]
    }
  ])
}

resource "aws_ecs_service" "api_gateway" {
  name            = "api-gateway-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api_gateway.arn
  desired_count   = 1 # Can be increased for high availability
  launch_type     = "FARGATE"
  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "api-gateway"
    container_port   = 8085
  }
  network_configuration {
    subnets         = aws_subnet.public[*].id
    security_groups = [aws_security_group.ecs_sg.id]
  }
}

resource "aws_ecs_task_definition" "user_service" {
  family                   = "user-service"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  container_definitions = jsonencode([
    {
      name      = "user-service"
      image     = "${local.ecr_base_url}/user-service:${var.image_tag}"
      essential = true
      portMappings = [{ containerPort = 8080 }]
      environment = [
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka-server.local:8761/eureka/" },
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.address}:5432/dynamic_car_sharing_db" },
        { name = "DB_USERNAME", value = "postgres" },
        { name = "DB_PASSWORD", value = var.db_password },
        { name = "APPLICATION_SECURITY_JWT_SECRET-KEY", value = var.jwt_secret_key }
      ]
    }
  ])
}

resource "aws_ecs_service" "user_service" {
  name            = "user-service-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.user_service.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  network_configuration {
    subnets         = aws_subnet.public[*].id
    security_groups = [aws_security_group.ecs_sg.id]
  }
  service_registries {
    registry_arn = aws_service_discovery_service.user.arn
  }
}

resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "local"
  vpc  = aws_vpc.main.id
}

resource "aws_service_discovery_service" "eureka" {
  name = "eureka-server"
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id
    dns_records {
      ttl  = 10
      type = "A"
    }
  }
}

resource "aws_service_discovery_service" "user" {
  name = "user-service"
  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id
    dns_records {
      ttl  = 10
      type = "A"
    }
  }
}