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
  var farmId: String = _
  var channel: String = _
  var channelVersion: Int = _
  var module: String = _
  var moduleVersion: Int = _
  var attempt: Int = _
  var addedAt: DateTime = _
  var lastStatusChange: DateTime = _

  /*
  Idk if this is really a good way of doing this...
   */
  def jobId : String = {
    var id: String = channelVersion.toString
    id += moduleVersion
    id += attempt
    id += farmId
    id += channel
    id += module
    id += addedAt.toString()
    id += lastStatusChange.toString()

    return id.hashCode.toHexString
  }
}
