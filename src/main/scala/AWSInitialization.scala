/**
 * User: cameron
 * Date: 4/14/14
 * Time: 3:26 PM
 * Creates all buckets and tables used by the DSS
 */
import awscala._, s3._, dynamodbv2._
import constants.{FarmChannelConstants, JobStatusTableConstants}
import dynamo.DynamoAccessor

object AWSInitialization extends DynamoAccessor {
  implicit val s3 = S3()

  def setup(prefix: String) = {
    ensureBucketExists(prefix + "farmbot-dss-rundata")
    ensureBucketExists(prefix + "farmbot-dss-chanels")

    ensureJobStatusTableExists(prefix)
    ensureChannelFarmTableExists(prefix)
  }

  private def ensureBucketExists(name: String) = {
    val bucket: Option[Bucket] = s3.bucket(name)

    if (bucket.isEmpty) {
      s3.createBucket(name)
    }
  }

  private def ensureJobStatusTableExists(prefix: String) = {
    implicit val const = JobStatusTableConstants

    val tableName: String = prefix + const.TABLE_NAME
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.JOB_ID -> const.JOB_ID_TYPE
      )
    }
  }

  private def ensureChannelFarmTableExists(prefix: String) = {
    implicit val const = FarmChannelConstants

    val tableName: String = prefix + const.TABLE_NAME
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.CHANNEL_AND_VERSION -> const.CHANNEL_AND_VERSION_TYPE
      )
    }
  }
}
