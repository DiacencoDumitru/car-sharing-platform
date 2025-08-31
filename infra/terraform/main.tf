terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_vpc" "vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = { Name = "dcs-vpc" }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id
}

data "aws_availability_zones" "azs" {}

resource "aws_subnet" "public_a" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = data.aws_availability_zones.azs.names[0]
  map_public_ip_on_launch = true
  tags = { Name = "dcs-public-a" }
}

resource "aws_subnet" "public_b" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = data.aws_availability_zones.azs.names[1]
  map_public_ip_on_launch = true
  tags = { Name = "dcs-public-b" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource "aws_route_table_association" "a" {
  route_table_id = aws_route_table.public.id
  subnet_id      = aws_subnet.public_a.id
}

resource "aws_route_table_association" "b" {
  route_table_id = aws_route_table.public.id
  subnet_id      = aws_subnet.public_b.id
}

resource "aws_security_group" "alb_sg" {
  name   = "dcs-alb-sg"
  vpc_id = aws_vpc.vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "svc_sg" {
  name   = "dcs-service-sg"
  vpc_id = aws_vpc.vpc.id

  ingress {
    from_port = 0
    to_port   = 65535
    protocol  = "tcp"
    self      = true
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_ecs_cluster" "this" {
  name = "dcs-ecs"
}

resource "aws_service_discovery_private_dns_namespace" "ns" {
  name        = "svc.local"
  description = "Service discovery for DCS"
  vpc         = aws_vpc.vpc.id
}

resource "aws_iam_role" "task_execution" {
  name = "dcs-ecsTaskExecutionRole"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [{
      Action    = "sts:AssumeRole",
      Principal = { Service = "ecs-tasks.amazonaws.com" },
      Effect    = "Allow"
    }]
  })
}

resource "aws_iam_role_policy_attachment" "exec_ecr" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_cloudwatch_log_group" "logs" {
  name              = "/ecs/dcs"
  retention_in_days = 14
}

resource "aws_lb" "alb" {
  name               = "dcs-alb"
  load_balancer_type = "application"
  subnets            = [aws_subnet.public_a.id, aws_subnet.public_b.id]
  security_groups    = [aws_security_group.alb_sg.id]
}

resource "aws_lb_target_group" "api_tg" {
  name        = "dcs-api-tg"
  port        = 8085
  protocol    = "HTTP"
  vpc_id      = aws_vpc.vpc.id
  target_type = "ip"

  health_check {
    path = "/actuator/health"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 80

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_tg.arn
  }
}

locals {
  services = {
    "eureka-server"   = { port = 8761, cpu = 256, mem = 512, desired = 1 }
    "api-gateway"     = { port = 8085, cpu = 256, mem = 512, desired = 2 }
    "user-service"    = { port = 8080, cpu = 256, mem = 512, desired = 2 }
    "car-service"     = { port = 8081, cpu = 256, mem = 512, desired = 2 }
    "booking-service" = { port = 8082, cpu = 256, mem = 512, desired = 2 }
    "dispute-service" = { port = 8083, cpu = 256, mem = 512, desired = 1 }
    "zipkin"          = { port = 9411, cpu = 256, mem = 512, desired = 1 }
  }
}

resource "aws_ecr_repository" "repos" {
  for_each = local.services
  name     = "dynamiccarsharing/${each.key}"
}

locals {
  common_env = [
    { name = "SPRING_PROFILES_ACTIVE", value = "jpa" },
    { name = "EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", value = "http://eureka-server.svc.local:8761/eureka/" },
    { name = "MANAGEMENT_TRACING_PROPAGATION_TYPE", value = "B3" },
    { name = "MANAGEMENT_TRACING_SAMPLING_PROBABILITY", value = "1.0" },
    { name = "MANAGEMENT_TRACING_ZIPKIN_TRACING_ENDPOINT", value = "http://zipkin.svc.local:9411/api/v2/spans" },
    { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${var.db_host}:5432/dynamic_car_sharing_db" },
    { name = "DB_USERNAME", value = var.db_user },
    { name = "DB_PASSWORD", value = var.db_password }
  ]
}

resource "aws_ecs_task_definition" "td" {
  for_each                 = local.services
  family                   = "dcs-${each.key}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = tostring(each.value.cpu)
  memory                   = tostring(each.value.mem)
  execution_role_arn       = aws_iam_role.task_execution.arn

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = "${var.ecr_registry}/dynamiccarsharing/${each.key}:${var.image_tag}"
      essential = true
      portMappings = [{
        containerPort = each.value.port
        hostPort      = each.value.port
        protocol      = "tcp"
      }]
      environment = local.common_env
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = aws_cloudwatch_log_group.logs.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = each.key
        }
      }
    }
  ])
}

resource "aws_service_discovery_service" "sd" {
  for_each = local.services
  name     = each.key

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.ns.id
    dns_records {
      ttl  = 5
      type = "A"
    }
    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_ecs_service" "internal" {
  for_each               = { for k, v in local.services : k => v if k != "api-gateway" }
  name                   = "dcs-${each.key}"
  cluster                = aws_ecs_cluster.this.id
  task_definition        = aws_ecs_task_definition.td[each.key].arn
  desired_count          = each.value["desired"]
  launch_type            = "FARGATE"
  enable_execute_command = false

  network_configuration {
    assign_public_ip = true
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.svc_sg.id]
  }

  service_registries {
    registry_arn = aws_service_discovery_service.sd[each.key].arn
  }
}

resource "aws_ecs_service" "api" {
  name            = "dcs-api-gateway"
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.td["api-gateway"].arn
  desired_count   = local.services["api-gateway"]["desired"]
  launch_type     = "FARGATE"

  network_configuration {
    assign_public_ip = true
    subnets          = [aws_subnet.public_a.id, aws_subnet.public_b.id]
    security_groups  = [aws_security_group.svc_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api_tg.arn
    container_name   = "api-gateway"
    container_port   = local.services["api-gateway"]["port"]
  }

  service_registries {
    registry_arn = aws_service_discovery_service.sd["api-gateway"].arn
  }

  depends_on = [aws_lb_listener.http]
}

output "alb_url" {
  value = aws_lb.alb.dns_name
}
