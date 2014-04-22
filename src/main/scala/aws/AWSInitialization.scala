package aws

/**
 * User: cameron
 * Date: 4/14/14
 * Time: 3:26 PM
 * Creates all buckets and tables used by the DSS
 */

import _root_.dynamo.DynamoAccessor
import awscala._, s3._, dynamodbv2._
import constants.{FarmChannelConstants, JobStatusTableConstants}

object AWSInitialization extends DynamoAccessor with UsesPrefix {
  implicit val s3 = S3()

  def setup = {
    ensureBucketExists(build("farmbot-dss-rundata"))
    ensureBucketExists(build("farmbot-dss-chanels"))

    ensureJobStatusTableExists
    ensureChannelFarmTableExists
  }

  private def ensureBucketExists(name: String) = {
    val bucket: Option[Bucket] = s3.bucket(name)

    if (bucket.isEmpty) {
      s3.createBucket(name)
    }
  }

  private def ensureJobStatusTableExists = {
    implicit val const = JobStatusTableConstants

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.JOB_ID -> const.JOB_ID_TYPE
      )
    }
  }

  private def ensureChannelFarmTableExists = {
    implicit val const = FarmChannelConstants

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.CHANNEL_AND_VERSION -> const.CHANNEL_AND_VERSION_TYPE,
        rangePK = const.FARM_ID -> const.FARM_ID_TYPE,
        otherAttributes = Seq(),
        indexes = Seq()
      )
    }
  }
}
