package types

class FarmChannel {
  var channel: String = _
  var version: Int = _
  var farmID: String = _
  var schedule: String = _

  def key: String = channel + "_" + version

  def id : String = {
    var id: String = channel
    id += farmID
    id += version.toString
    id += schedule

    id.hashCode.toHexString
  }
}
