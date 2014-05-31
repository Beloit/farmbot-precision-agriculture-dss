package dynamo

import types.{Module, JobInfo}
import awscala._
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants
import constants.JobStatusTableConstants.JobStatus
import constants.JobStatusTableConstants.JobStatus.JobStatus
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue
import awscala.dynamodbv2.{ProvisionedThroughput, KeyType, KeySchema, Table}
import java.util

class JobStatusAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = JobStatusTableConstants

  ensureJobStatusTableExists

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addEntry(info: JobInfo): String = {
    info.addedAt = DateTime.now
    info.lastStatusChange = info.addedAt

    table.putItem(info.farmChannelId,
      info.jobId,
      const.FARM_ID -> info.farmId,
      const.RESOURCE_ID -> info.resourceId,
      const.CHANNEL -> info.channel,
      const.CHANNEL_VERSION -> info.channelVersion,
      const.MODULE -> info.module.name,
      const.MODULE_VERSION -> info.module.version,
      const.STATUS -> JobStatus.Pending.toString,
      const.ADDED_AT -> info.addedAt.toString(ISODateTimeFormat.dateTime()),
      const.LAST_STATUS_CHANGE -> info.lastStatusChange.toString(ISODateTimeFormat.dateTime()),
      const.NEXT_ID -> info.nextId.getOrElse(null),
      const.PREVIOUS_ID -> info.previousId.getOrElse(null)
    )

    return info.jobId
  }

  def updateStatus(job: JobInfo, newStatus: JobStatus, expectedStatus: JobStatus) {
    updateStatus(job.farmChannelId, job.jobId, newStatus, expectedStatus)
  }

  def updateStatus(farmChannelId: String, jobId: String, newStatus: JobStatus, expectedStatus: JobStatus) {
    val timestamp = DateTime.now().toString(ISODateTimeFormat.dateTime())

    val updateRequest = new UpdateItemRequest()
      .withTableName(table.name)

    updateRequest.addKeyEntry(const.FARM_CHANNEL_ID, new AttributeValue(farmChannelId))
    updateRequest.addKeyEntry(const.JOB_ID, new AttributeValue(jobId))

    updateRequest.addAttributeUpdatesEntry(const.STATUS, new AttributeValueUpdate(new AttributeValue(newStatus.toString), AttributeAction.PUT))
    updateRequest.addAttributeUpdatesEntry(const.LAST_STATUS_CHANGE, new AttributeValueUpdate(new AttributeValue(timestamp), AttributeAction.PUT))

    updateRequest.addExpectedEntry(const.STATUS, new ExpectedAttributeValue().withValue(new AttributeValue(expectedStatus.toString)).withComparisonOperator(ComparisonOperator.EQ))

    dynamo.updateItem(updateRequest)
  }

  def findReadyJob : Option[JobInfo] = {
    val queryRequest :QueryRequest = new QueryRequest()
      .withTableName(table.name)
      .withIndexName(const.STATUS + "-index")
      .withLimit(1)
      .withSelect("ALL_ATTRIBUTES")
      .withKeyConditions(new util.HashMap[String, Condition])

      queryRequest.getKeyConditions.put(const.STATUS, new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(JobStatus.Ready.toString)))

    val result: QueryResult = dynamo.query(queryRequest)

    if (result.getCount() == 1) {
      val jobInfoMap = result.getItems.get(0)
      val job: JobInfo = new JobInfo

      job.farmId = jobInfoMap.get(const.FARM_ID).getS
      job.farmChannelId = jobInfoMap.get(const.FARM_CHANNEL_ID).getS
      job.resourceId = jobInfoMap.get(const.RESOURCE_ID).getS
      job.channel = jobInfoMap.get(const.CHANNEL).getS
      job.channelVersion = jobInfoMap.get(const.CHANNEL_VERSION).getN.toInt

      if (jobInfoMap.get(const.PREVIOUS_ID) != null) {
        job.previousId = Option.apply(jobInfoMap.get(const.PREVIOUS_ID).getS)
      }

      if (jobInfoMap.get(const.NEXT_ID) != null) {
        job.nextId = Option.apply(jobInfoMap.get(const.NEXT_ID).getS)
      }

      job.module = new Module(){
        name = jobInfoMap.get(const.MODULE).getS
        version = jobInfoMap.get(const.MODULE_VERSION).getN.toInt
      }

      job.addedAt = DateTime.parse(jobInfoMap.get(const.ADDED_AT).getS, ISODateTimeFormat.dateTime())
      job.lastStatusChange = DateTime.parse(jobInfoMap.get(const.LAST_STATUS_CHANGE).getS, ISODateTimeFormat.dateTime())

      return Option.apply(job)
    } else {
      return Option.empty
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
}
