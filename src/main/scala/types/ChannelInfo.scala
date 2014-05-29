package types

import argonaut._, Argonaut._

/*
 * ChannelInfo class contains information about a channel for farmbot.
 * This as of now is a name, version, runFrequency (daily, weekly, monthly, etc),
 * list of modules, and metadata in the form of a map with key/value pairs.
 */
class ChannelInfo {
  var name : String = _
  var version : Int = _
  var metadata : Map[String, String] = _
  var modules : List[Module] = _
  var initialInput : String = _
  var schema : String = _
  
  /*
   * Currently this function is used to verify that a channel info object can be
   * converted to json, written to s3, read back from s3, then converted back
   * to a ChannelInfo object and be equal to the original. If this ends up being
   * the only time we use this function we can move it to a test class, but thought
   * it might end up being used for something else
   */
  def equals(other : ChannelInfo): Boolean = {
    if (!other.name.equals(name)) {
      println("names not equal")
      return false;
    } else if (!other.version.equals(version)) {
      println("versions not equal")
      return false
    } else if (!other.metadata.equals(metadata)) {
      println("metadata not equal")
      return false;
    } else if (!other.modules.size.equals(modules.size)) {
      println("modules size not equal")
      return false;
    } else {
      var x : Int = 1;
      for (x <- 1 until modules.size) {
        if (!other.modules(x).equals(modules(x))) {
          println("module " + x + " not equal")
          return false;
        }
      }
    }
    
    return true;
  }
}

