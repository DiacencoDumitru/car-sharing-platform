data "aws_caller_identity" "current" {}

locals {
  ecr_repo_arn_prefix = "arn:aws:ecr:${var.aws_region}:${data.aws_caller_identity.current.account_id}:repository/dynamiccarsharing/*"
}

resource "aws_iam_user" "gitlab_ci" {
  name = "dcs-gitlab-ci"
  tags = { Project = "dynamiccarsharing", Purpose = "GitLab CI" }
}

resource "aws_iam_policy" "ecr_push" {
  name        = "DCS-GitLab-ECRPush"
  description = "GitLab CI user can log in to ECR and push images to dynamiccarsharing/*"
  policy      = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "EcrLogin",
      "Effect": "Allow",
      "Action": "ecr:GetAuthorizationToken",
      "Resource": "*"
    },
    {
      "Sid": "PushToOurReposOnly",
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload",
        "ecr:PutImage",
        "ecr:DescribeRepositories",
        "ecr:CreateRepository"
      ],
      "Resource": "${local.ecr_repo_arn_prefix}"
    },
    {
      "Sid": "StsIdentity",
      "Effect": "Allow",
      "Action": "sts:GetCallerIdentity",
      "Resource": "*"
    }
  ]
}
POLICY
}

resource "aws_iam_user_policy_attachment" "attach" {
  user       = aws_iam_user.gitlab_ci.name
  policy_arn = aws_iam_policy.ecr_push.arn
}

resource "aws_iam_access_key" "gitlab_ci" {
  user = aws_iam_user.gitlab_ci.name
}

output "gitlab_ci_access_key_id" {
  value = aws_iam_access_key.gitlab_ci.id
}

output "gitlab_ci_secret_access_key" {
  value     = aws_iam_access_key.gitlab_ci.secret
  sensitive = true
}
