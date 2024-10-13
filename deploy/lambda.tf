locals {
  s3_bucket = "FILL_ME_IN"
  s3_key = "FILL_ME_IN"
  s3_object_version = "FILL_ME_IN"
}

data aws_iam_policy_document event_search_assume_role {
  statement {
    effect = "Allow"
    principals {
      type = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    actions = ["sts:AssumeRole"]
  }
}

resource aws_secretsmanager_secret ticketmaster_api_key {
  name = "ticketmaster_api_key"
}

resource aws_iam_role event_search_execution_role {
  name = "event_search_execution_role"
  assume_role_policy = data.aws_iam_policy_document.event_search_assume_role.json
  inline_policy {
    name = "event-search-lambda-inline-policy"
    policy = jsonencode({
      Version = "2012-10-17"
      Statement = [
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
      ]
    })
  }
}

data aws_s3_object event_search_lambda_jar {
  bucket = local.s3_bucket
  key    = "event_search_lambda.jar"
}

resource aws_security_group event_search_sg {
  name = "event_search_sg"
  vpc_id = aws_vpc.main.id
}

// Terraform removes the allow all egress rule from security groups by default
resource aws_vpc_security_group_egress_rule allow_all_egress_ipv4 {
  security_group_id = aws_security_group.event_search_sg.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1" # semantically equivalent to all ports
}

resource aws_lambda_function event_search {
  function_name                  = "ticketmaster-event-search"
  s3_bucket                      = local.s3_bucket
  s3_key                         = data.aws_s3_object.event_search_lambda_jar.key
  s3_object_version              = data.aws_s3_object.event_search_lambda_jar.version_id
  #   layers                         = var.layers
  handler                        = "org.example.lambda.EventSearchHandler"
  role                           = aws_iam_role.event_search_execution_role.arn
  timeout                        = 5 // value in seconds
  runtime                        = "java21"
  architectures                  = ["x86_64"]
  memory_size                    = 256
  logging_config {
    log_format = "Text"
  }
  environment {
    variables = {
      API_KEY_ARN = aws_secretsmanager_secret.ticketmaster_api_key.arn
    }
  }
  vpc_config {
    security_group_ids = [aws_security_group.event_search_sg.id]
    subnet_ids         = [aws_subnet.private.id]
  }
}