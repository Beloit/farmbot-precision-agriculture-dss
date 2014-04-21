package dynamo

import types.JobInfo
import awscala._, dynamodbv2._
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 5:09 PM
 * Accessor for the JobStatus table
 */
class FarmChannelAccessor(val prefix: String) extends DynamoAccessor {
  implicit val const = JobStatusTableConstants

  var table: Table = dynamo.table(prefix + const.TABLE_NAME) get

  def addEntry(info: JobInfo) {
    val timestamp = DateTime.now().toString(ISODateTimeFormat.dateTime())

    println(table.hashCode())

    table.putItem(info.jobId,
      const.FARM_ID -> info.farmId,
      const.CHANNEL -> info.channel,
      const.CHANNEL_VERSION -> info.channelVersion,
      const.MODULE -> info.module,
      const.MODULE_VERSION -> info.moduleVersion,
      const.STATUS -> const.PENDING_STATUS,
      const.ADDED_AT -> timestamp,
      const.LAST_STATUS_CHANGE -> timestamp
    )
  }

  def updateStatus(id: Int, status: String) {
    /*
    Up next
     */
  }
}
