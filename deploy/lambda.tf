locals { path_to_jar = "../target/KpopAlerts-1.0-SNAPSHOT-jar-with-dependencies.jar" }

resource "aws_s3_bucket" "build_artifacts" {
  #   bucket = "kpop-alerts-build-artifacts-private"
  // Only for sandbox development
  bucket = "tmp-delete-at-will-dash-s3-bucket"
}

resource "aws_s3_object" "kpop_alerts_jar" {
  bucket = aws_s3_bucket.build_artifacts.id
  key    = "event-search-lambda.jar"
  // Only for sandbox development
  source = local.path_to_jar
}

resource "aws_secretsmanager_secret" "ticketmaster_api_key" {
  #   name = "ticketmaster_api_key"
  name = "ticketmaster_api_key_tmp"
}

resource "aws_secretsmanager_secret_version" "ticketmaster_api_key_v1" {
  // Test key from Ticketmaster docs
  secret_id     = aws_secretsmanager_secret.ticketmaster_api_key.id
  secret_string = "ArczcVznMlEZoupJgqHpkWuo1ASxGEGA"
}

data "aws_iam_policy_document" "event_search_assume_role" {
  statement {
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role" "event_search_execution_role" {
  name               = "event_search_execution_role"
  assume_role_policy = data.aws_iam_policy_document.event_search_assume_role.json
}

resource "aws_iam_policy" "lambda_execution_role_policy" {
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        "Effect" : "Allow",
        "Action" : [
          "ec2:DescribeNetworkInterfaces",
          "ec2:CreateNetworkInterface",
          "ec2:DeleteNetworkInterface",
          "ec2:DescribeInstances",
          "ec2:AttachNetworkInterface"
        ],
        "Resource" : "*"
      },
      {
        Effect = "Allow"
        Action = [
          "sns:Publish"
        ]
        Resource = [
          aws_sns_topic.user_updates.arn
        ]
      },
      {
        Action   = ["secretsmanager:GetSecretValue"],
        Effect   = "Allow",
        Resource = aws_secretsmanager_secret.ticketmaster_api_key.arn
      },
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = [
          aws_cloudwatch_log_group.event_search_log_group.arn,
          "${aws_cloudwatch_log_group.event_search_log_group.arn}:*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_execution_attach_policy" {
  role       = aws_iam_role.event_search_execution_role.name
  policy_arn = aws_iam_policy.lambda_execution_role_policy.arn
}

resource "aws_security_group" "event_search_sg" {
  name   = "event_search_sg"
  vpc_id = aws_vpc.main.id
}

// Terraform removes the allow all egress rule from security groups by default
resource "aws_vpc_security_group_egress_rule" "allow_all_egress_ipv4" {
  security_group_id = aws_security_group.event_search_sg.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1" # semantically equivalent to all ports
}

resource "aws_lambda_function" "event_search" {
  function_name     = "ticketmaster-event-search"
  s3_bucket         = aws_s3_bucket.build_artifacts.id
  s3_key            = aws_s3_object.kpop_alerts_jar.key
  s3_object_version = aws_s3_object.kpop_alerts_jar.version_id
  source_code_hash  = filebase64sha256(local.path_to_jar)
  #   layers                         = var.layers
  handler       = "org.example.lambda.EventSearchHandler"
  role          = aws_iam_role.event_search_execution_role.arn
  timeout       = 30 // value in seconds
  runtime       = "java21"
  architectures = ["x86_64"]
  memory_size   = 256
  depends_on = [aws_cloudwatch_log_group.event_search_log_group]
  logging_config {
    log_format = "Text"
  }
  environment {
    variables = {
      API_KEY_ARN   = aws_secretsmanager_secret.ticketmaster_api_key.arn
      SNS_TOPIC_ARN = aws_sns_topic.user_updates.arn
    }
  }
  vpc_config {
    security_group_ids = [aws_security_group.event_search_sg.id]
    subnet_ids         = [aws_subnet.private.id]
  }
}

resource "aws_cloudwatch_log_group" "event_search_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.event_search.function_name}"
  retention_in_days = 7
}
