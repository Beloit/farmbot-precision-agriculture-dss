package types

import argonaut._, Argonaut._

/*
 * Module object defines a module within channel for farmbot. Includes a
 * name and version. We can add more fields that are needed for a module
 * if needed.
 */
class Module {
  var name : String = _
  var version : Int = _
  
  /*
   * Equals function that is used currently to test (see comment for ChannelInfo
   * equals method). We can move this function if we end up only ever using it 
   * for testing
   */
  def equals(other : Module): Boolean = {
    return name.equals(other.name) && version.equals(other.version)
  }
}