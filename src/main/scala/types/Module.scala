package types

/*
 * Module object defines a module within channel for farmbot. Includes a
 * name and version. We can add more fields that are needed for a module
 * if needed.
 */
class Module {
  var name : String = _
  var version : Int = _

  val KEY_FORMAT: String = "%s_%s"

  /*
   * Equals function that is used currently to test (see comment for ChannelInfo
   * equals method). We can move this function if we end up only ever using it 
   * for testing
   */
  def equals(other : Module): Boolean = {
    return name.equals(other.name) && version.equals(other.version)
  }

  def ==(other : Module): Boolean = equals(other)

  def key: String = return String.format(KEY_FORMAT, name, version.toString)
}