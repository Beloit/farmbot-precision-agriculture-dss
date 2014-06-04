package types

class FarmChannel {
  var channel: String = _
  var version: Int = _
  var farmID: String = _
  var scheduleHour: Option[Int] = Option.empty
  var scheduleMinute: Option[Int] = Option.empty
  var lastRunTime: Long = _

  private var opaqueIdentifierOption: Option[String] = Option.empty

  def opaqueIdentifier: String = {
    if (opaqueIdentifierOption.isEmpty) {
      generateOpaqueIdentifier
    } else {
      opaqueIdentifierOption.get
    }
  }

  def setOpaqueIdentifier(key: String) {
    opaqueIdentifierOption = Option.apply(key)
  }

  def key: String = channel + "_" + version

  /*
  Used to generate a key to be used as the primary key in the resource table accessor
   */
  private def generateOpaqueIdentifier : String = {
    var id: String = channel
    id += farmID
    id += version.toString
    id += scheduleHour
    id += scheduleMinute

    id.hashCode.toHexString
  }
}
