package types

class FarmChannel {
  var channel: String = _
  var version: Int = _
  var farmID: String = _
  var scheduleHour: String = _
  var scheduleMinute: String = _
  
  def key: String = channel + "_" + version

  def id : String = {
    var id: String = channel
    id += farmID
    id += version.toString
    id += scheduleHour
    id += scheduleMinute

    id.hashCode.toHexString
  }
}
