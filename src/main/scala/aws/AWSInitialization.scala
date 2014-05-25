package aws

import _root_.dynamo.DynamoAccessor
import awscala._, dynamodbv2._
import awscala.s3.S3
import awscala.s3.Bucket
import constants.{ModuleConstants, FarmChannelConstants, JobStatusTableConstants}

object AWSInitialization extends DynamoAccessor with UsesPrefix {
  implicit val s3 = S3()

  def setup = {
    ensureBucketExists(build("farmbot-dss-rundata"))
    ensureBucketExists(build("farmbot-dss-chanels"))

    ensureJobStatusTableExists
    ensureChannelFarmTableExists
    ensureModuleConfigurationTableExists
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
        hashPK = const.FARM_CHANNEL_ID -> const.FARM_CHANNEL_ID_TYPE,
        rangePK = const.JOB_ID -> const.JOB_ID_TYPE,
        otherAttributes = Seq(),
        indexes = Seq()
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

  private def ensureModuleConfigurationTableExists = {
    implicit val const = ModuleConstants

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.MODULE_NAME_VERSION -> const.MODULE_NAME_VERSION_TYPE
      )
    }
  }
}
