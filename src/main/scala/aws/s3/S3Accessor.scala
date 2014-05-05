package s3

import awscala.s3.S3

trait S3Accessor {
  implicit val s3 = S3()
}