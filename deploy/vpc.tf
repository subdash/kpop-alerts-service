resource aws_vpc main {
  cidr_block = "10.0.0.0/16"
}

resource aws_subnet public {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
}

resource aws_subnet private {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.2.0/24"
}

resource aws_internet_gateway igw {
  vpc_id = aws_vpc.main.id
}

resource aws_default_route_table default_route_table {
  vpc_id         = aws_vpc.main.id
  default_route_table_id = aws_vpc.main.default_route_table_id
  route {
    cidr_block = "10.0.1.0/24"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource aws_route_table secondary_route_table {
  vpc_id = aws_vpc.main.id
}