package s3

import awscala.s3.{Bucket, S3}

trait S3Accessor {
  implicit val s3 = S3()

  def ensureBucketExists(name: String) = {
    val bucket: Option[Bucket] = s3.bucket(name)

    if (bucket.isEmpty) {
      s3.createBucket(name)
    }
  }
}