resource "aws_sns_topic" "user_updates" {
  name = "upcoming-kpop-shows-topic"
}

resource "aws_sns_topic_subscription" "test_sns_subscriber" {
  endpoint  = "fake-email@gmail.com"
  protocol  = "email"
  topic_arn = aws_sns_topic.user_updates.arn
}
