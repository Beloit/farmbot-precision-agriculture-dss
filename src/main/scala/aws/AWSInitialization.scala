package aws

import _root_.dynamo.DynamoAccessor
import _root_.s3.S3Accessor
import awscala._, dynamodbv2._
import awscala.s3.Bucket
import constants.{ModuleConstants, FarmChannelConstants, JobStatusTableConstants, ResourceConstants}
import com.amazonaws.services.dynamodbv2.model.{GlobalSecondaryIndex, KeySchemaElement, CreateTableRequest, AttributeDefinition, Projection, ProjectionType}
import java.util

object AWSInitialization extends DynamoAccessor with UsesPrefix with S3Accessor {
  def setup = {
    ensureBucketExists(build(ModuleConstants.BUCKET_NAME))
    ensureBucketExists(build("farmbot-dss-rundata"))
    ensureBucketExists(build("farmbot-dss-chanels"))

    ensureJobStatusTableExists
    ensureChannelFarmTableExists
    ensureModuleConfigurationTableExists
    ensureResourceTableExists
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
      val createTableRequest = new CreateTableRequest
      createTableRequest.setTableName(tableName)

      val keySchema = new util.ArrayList[KeySchemaElement]
      keySchema.add(new KeySchema(const.FARM_CHANNEL_ID, KeyType.Hash))
      keySchema.add(new KeySchema(const.JOB_ID, KeyType.Range))
      createTableRequest.setKeySchema(keySchema)

      val attributes = new util.ArrayList[AttributeDefinition]
      attributes.add(new AttributeDefinition(const.FARM_CHANNEL_ID, const.FARM_CHANNEL_ID_TYPE))
      attributes.add(new AttributeDefinition(const.JOB_ID, const.JOB_ID_TYPE))
      attributes.add(new AttributeDefinition(const.STATUS, const.STATUS_TYPE))
      createTableRequest.setAttributeDefinitions(attributes)

      val globalSecondaryIndex = new GlobalSecondaryIndex
      globalSecondaryIndex.setKeySchema(new util.ArrayList[KeySchemaElement]())
      globalSecondaryIndex.getKeySchema.add(new KeySchema(const.STATUS, KeyType.Hash))

      globalSecondaryIndex.setIndexName(const.STATUS + "-index")
      globalSecondaryIndex.setProjection(new Projection().withProjectionType(ProjectionType.ALL))
      globalSecondaryIndex.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L))

      createTableRequest.setGlobalSecondaryIndexes(new util.ArrayList[GlobalSecondaryIndex]())
      createTableRequest.getGlobalSecondaryIndexes.add(globalSecondaryIndex)

      createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L))

      dynamo.createTable(createTableRequest)
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

      Thread.sleep(10000)

      dynamo.table(tableName).get.update(ProvisionedThroughput(1L, 1L))
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

      Thread.sleep(10000)

      dynamo.table(tableName).get.update(ProvisionedThroughput(1L, 1L))
    }
  }
  
  private def ensureResourceTableExists = {
    implicit val const = ResourceConstants

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.FARM_CHANNEL_ID -> const.FARM_CHANNEL_ID_TYPE,
        rangePK = const.RESOURCE_ID -> const.RESOURCE_ID_TYPE,
        otherAttributes = Seq(),
        indexes = Seq()
      )
    }
  }
}
