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
import awscala.dynamodbv2.Table
import java.util

class JobStatusAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = JobStatusTableConstants

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
      const.NEXT_ID -> info.nextId,
      const.PREVIOUS_ID -> info.previousId
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
      job.resourceId = jobInfoMap.get(const.RESOURCE_ID).getS
      job.channel = jobInfoMap.get(const.CHANNEL).getS
      job.channelVersion = jobInfoMap.get(const.CHANNEL_VERSION).getN.toInt
      job.previousId = jobInfoMap.get(const.PREVIOUS_ID).getS
      job.nextId = jobInfoMap.get(const.NEXT_ID).getS
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
}
