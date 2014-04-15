package types

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 4:58 PM
 * Contains information required for a Job's status
 */
class JobInfo {
  var farmId: String = _
  var jobId: Int = _
  var channel: String = _
  var channelVersion: Int = _
  var module: String = _
  var moduleVersion: Int = _
  var attempt: Int = _
}
