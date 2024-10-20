resource "aws_cloudwatch_event_rule" "event_search_schedule" {
  name                = "event-search-schedule"
  schedule_expression = "cron(0 13 ? * 3,5 *)" // 8am CST daily
}

// Give event bridge permission to invoke Lambda
resource "aws_lambda_permission" "allow_event_bridge_to_run_lambda_cnd" {
  statement_id  = "AllowEventSearchExecutionFromCloudwatch"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.event_search.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.event_search_schedule.arn
}

resource "aws_cloudwatch_event_target" "event_search_target" {
  arn  = aws_lambda_function.event_search.arn
  rule = aws_cloudwatch_event_rule.event_search_schedule.name
}