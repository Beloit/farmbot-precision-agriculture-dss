package types

import org.joda.time.DateTime

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 4:58 PM
 * Contains information required for a Job's status
 */
class JobInfo {
  var farmChannelId: String = _
  var farmId: String = _
  var resourceId: String = _
  var channel: String = _
  var channelVersion: Int = _
  var module: Module = _
  var attempt: Int = 0
  var addedAt: DateTime = _
  var lastStatusChange: DateTime = _
  var nextId: Option[String] = Option.empty
  var previousId: Option[String] = Option.empty

  /*
  Idk if this is really a good way of doing this...
   */
  def jobId : String = {
    var id: String = channelVersion.toString
    id += farmChannelId
    id += module.toString()
    id += farmId
    id += resourceId
    id += channel
    id += addedAt.toString()

    return id.hashCode.toHexString
  }

  def copy : JobInfo = {
    val ji = new JobInfo
    ji.farmId = farmId
    ji.channel = channel
    ji.channelVersion = channelVersion
    ji.module = module
    ji.attempt = attempt
    ji.addedAt = addedAt
    ji.lastStatusChange = lastStatusChange
    ji.resourceId = resourceId
    ji.nextId = nextId
    ji.previousId = previousId

    ji
  }

  override def toString : String = {
    String.format("farmID: %s, resourceID: %s, channel: %s, version: %s, module: %s, attempt: %s",
      farmId, resourceId, channel, channelVersion.toString, module.toString, attempt.toString)
  }
}
