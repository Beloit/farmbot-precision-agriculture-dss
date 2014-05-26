package dynamo

import types.JobInfo
import awscala._, dynamodbv2._
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants
import constants.JobStatusTableConstants.JobStatus
import constants.JobStatusTableConstants.JobStatus.JobStatus
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.Condition
import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 5:09 PM
 * Accessor for the JobStatus table
 */
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

  def findReadyJob : JobInfo = {
    /*stupid scala aws doesn't support queries well*/
    /*table.queryWithIndex(LocalSecondaryIndex(const.STATUS, Seq(), null), Seq())*/
    null
  }
}
