locals { path_to_index = "../html/index.html" }

resource "aws_s3_bucket" "static_site" {
  #   bucket = "kpop-alerts-build-artifacts-private"
  // Only for sandbox development
  bucket = "tmp-delete-at-will-dash-s3-website-bucket"

}

resource "aws_s3_object" "static_site_index" {
  bucket = aws_s3_bucket.static_site.id
  key    = "index.html"
  // Only for sandbox development
  source       = local.path_to_index
  etag         = filemd5(local.path_to_index)
  content_type = "text/html"
}

resource "aws_s3_bucket_website_configuration" "static_site" {
  bucket = aws_s3_bucket.static_site.id

  index_document {
    suffix = "index.html"
  }
}

resource "aws_s3_bucket_public_access_block" "example" {
  bucket = aws_s3_bucket.static_site.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_policy" "allow_public_access" {
  bucket = aws_s3_bucket.static_site.id
  policy = data.aws_iam_policy_document.allow_public_access.json
}

data "aws_iam_policy_document" "allow_public_access" {
  statement {
    principals {
      type        = "*"
      identifiers = ["*"]
    }

    actions = [
      "s3:GetObject",
    ]

    resources = [
      aws_s3_bucket.static_site.arn,
      "${aws_s3_bucket.static_site.arn}/*",
    ]
  }
}
