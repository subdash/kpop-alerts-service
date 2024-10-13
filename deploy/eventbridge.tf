resource "aws_cloudwatch_event_rule" "event_search_schedule" {
  name                = "event-search-schedule"
  schedule_expression = "cron(0 13 ? * 3 *)" // 8am CST each Tuesday
}

resource "aws_cloudwatch_event_target" "event_search_target" {
  arn  = aws_lambda_function.event_search.arn
  rule = aws_cloudwatch_event_rule.event_search_schedule.name
}