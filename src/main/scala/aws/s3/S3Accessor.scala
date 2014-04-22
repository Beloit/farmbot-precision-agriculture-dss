package s3

import awscala._, s3._, dynamodbv2._

trait S3Accessor {
  implicit val s3 = S3()
}