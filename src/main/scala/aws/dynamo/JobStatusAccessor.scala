package dynamo

import types.{Module, JobInfo}
import awscala._, dynamodbv2._
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants
import constants.JobStatusTableConstants.JobStatus
import constants.JobStatusTableConstants.JobStatus.JobStatus
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.{QueryResult, ComparisonOperator, QueryRequest, Condition}
import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2
import java.util

class JobStatusAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = JobStatusTableConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addEntry(info: JobInfo): String = {
    info.addedAt = DateTime.now
    info.lastStatusChange = info.addedAt

    table.putItem(info.jobId,
      const.FARM_ID -> info.farmId,
      const.RESOURCE_ID -> info.resourceId,
      const.CHANNEL -> info.channel,
      const.CHANNEL_VERSION -> info.channelVersion,
      const.MODULE -> info.module.name,
      const.MODULE_VERSION -> info.module.version,
      const.STATUS -> JobStatus.Pending.toString,
      const.ADDED_AT -> info.addedAt.toString(ISODateTimeFormat.dateTime()),
      const.LAST_STATUS_CHANGE -> info.lastStatusChange.toString(ISODateTimeFormat.dateTime())
    )

    return info.jobId
  }

  def updateStatus(jobId: String, status: JobStatus) {
    val timestamp = DateTime.now().toString(ISODateTimeFormat.dateTime())

    table.putAttributes(jobId,
      Seq(const.STATUS -> status.toString,const.LAST_STATUS_CHANGE -> timestamp)
    )
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
