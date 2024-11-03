resource "aws_api_gateway_rest_api" "event_search_apigw" {
  name = "EventSearchAPI"
}

resource "aws_api_gateway_deployment" "event_search_api_deployment" {
  rest_api_id = aws_api_gateway_rest_api.event_search_apigw.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.subscribe.id,
      aws_api_gateway_method.subscribe_post.id,
      aws_api_gateway_integration.integration.id,
      aws_api_gateway_integration_response.proxy.id,
      aws_api_gateway_method_response.response_201.id,
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "event_search_api_prd_stage" {
  deployment_id = aws_api_gateway_deployment.event_search_api_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.event_search_apigw.id
  stage_name    = "prd"
}

resource "aws_api_gateway_resource" "subscribe" {
  rest_api_id = aws_api_gateway_rest_api.event_search_apigw.id
  parent_id   = aws_api_gateway_rest_api.event_search_apigw.root_resource_id
  path_part   = "subscribe"
}

resource "aws_api_gateway_method" "subscribe_post" {
  rest_api_id   = aws_api_gateway_rest_api.event_search_apigw.id
  resource_id   = aws_api_gateway_resource.subscribe.id
  http_method   = "POST"
  authorization = "NONE"
  request_models = {
    "application/json" = aws_api_gateway_model.subscribe_req_model.name
  }
}

resource "aws_api_gateway_integration" "integration" {
  rest_api_id             = aws_api_gateway_rest_api.event_search_apigw.id
  resource_id             = aws_api_gateway_resource.subscribe.id
  http_method             = aws_api_gateway_method.subscribe_post.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.subscribe.invoke_arn
}

resource "aws_api_gateway_method_response" "response_201" {
  rest_api_id = aws_api_gateway_rest_api.event_search_apigw.id
  resource_id = aws_api_gateway_resource.subscribe.id
  http_method = aws_api_gateway_method.subscribe_post.http_method
  status_code = "201"
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true,
    "method.response.header.Access-Control-Allow-Methods" = true,
    "method.response.header.Access-Control-Allow-Origin"  = true
  }
  response_models = {
    "application/json" = aws_api_gateway_model.subscribe_res_model.name
  }
}

resource "aws_api_gateway_integration_response" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.event_search_apigw.id
  resource_id = aws_api_gateway_resource.subscribe.id
  http_method = aws_api_gateway_method.subscribe_post.http_method
  status_code = aws_api_gateway_method_response.response_201.status_code
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
    "method.response.header.Access-Control-Allow-Methods" = "'GET,OPTIONS,POST,PUT'",
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
  }
}

resource "aws_api_gateway_model" "subscribe_req_model" {
  rest_api_id  = aws_api_gateway_rest_api.event_search_apigw.id
  name         = "SubscribeApiRequest"
  description  = "subscribe request POST body"
  content_type = "application/json"
  schema = jsonencode({
    type = "object"
    properties = {
      email = {
        type = "string"
      }
    }
  })
}

resource "aws_api_gateway_model" "subscribe_res_model" {
  rest_api_id  = aws_api_gateway_rest_api.event_search_apigw.id
  name         = "SubscribeApiResponse"
  description  = "subscribe response body"
  content_type = "application/json"
  schema = jsonencode({
    type     = "object"
    required = ["created"]
    properties = {
      created = {
        type = "boolean"
      }
      error = {
        type = "string"
      }
    }
  })
}
