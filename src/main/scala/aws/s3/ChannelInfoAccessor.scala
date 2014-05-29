package s3

import argonaut._, Argonaut._
import types.ChannelInfo
import awscala._
import awscala.s3._
import sys.process._
import java.io.File
import types.ChannelInfo
import types.Module
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import aws.UsesPrefix

/*
 * Accessor for farmbot channel info from s3. 
 */
class ChannelInfoAccessor extends S3Accessor with UsesPrefix {
  /*
   * Defines how to encode a Module object to json, all of these functions are
   * needed for the Argonaut json library
   */
  implicit def ModuleCodecJson: EncodeJson[Module] = 
    EncodeJson((module : Module) =>
      ("name" := module.name) ->: ("version" := module.version) ->: ("persistent" := module.persistent)
      ->: ("timeout" := module.timeout) ->: jEmptyObject)
  
  /*
   * Defines how to encode a ChannelInfo object to json
   */
  implicit def ChannelInfoEncodeJson: EncodeJson[ChannelInfo] = 
    EncodeJson((info : ChannelInfo) =>
      ("name" := info.name) ->: ("version" := info.version) ->: ("metadata" := info.metadata)
        ->: ("modules" := info.modules) ->: ("initialInput" := info.initialInput) 
        ->: ("schema" := info.schema) ->: jEmptyObject)
        
  /*
   * Defines how to decode json to a ChannelInfo object 
   */
  implicit def ChannelInfoDecodeJson: DecodeJson[ChannelInfo] = 
    DecodeJson(c => for {
      channelName <- (c --\ "name").as[String]
      channelVersion <- (c --\ "version").as[Int]
      channelMetadata <- (c --\ "metadata").as[Map[String, String]]
      channelModules <- (c --\ "modules").as[List[Module]]
      channelInitialInput <- (c --\ "initialInput").as[String]
      channelSchema <- (c --\ "schema").as[String]
      
    } yield new ChannelInfo() {
      name = channelName
      version = channelVersion
      metadata = channelMetadata
      modules = channelModules
      initialInput = channelInitialInput
      schema = channelSchema
    }) 
    
  /*
   * Defines how to decode a Module object from json
   */
  implicit def ModuleDecodeJson: DecodeJson[Module] =
    DecodeJson(c => for {
      moduleName <- (c --\ "name").as[String]
      moduleVersion <- (c --\ "version").as[Int]
      modulePersistent <- (c --\ "persistent").as[Boolean]
      moduleTimeout <- (c --\ "timeout").as[Int]
    } yield new Module() {
      name = moduleName
      version = moduleVersion
      persistent = modulePersistent
      timeout = moduleTimeout
    })
     
   /*
    * Writes a ChannelInfo object as json to s3, uses s3 key of the form
    * "channelName/version"
    */
   def writeChannelData(info : ChannelInfo) {
     val key : String = info.name + "/" + info.version
     val bucket: Option[Bucket] = s3.bucket(build("farmbot-dss-chanels"))
     
     val json : Json = info.asJson
     val prettyprinted: String = json.spaces2
     var jsonString : String = info.name + "_" + info.version + " " + prettyprinted
     
     BasicIO.transferFully(new ByteArrayInputStream(prettyprinted.getBytes("UTF-8")), new FileOutputStream("tempfile"))
     val file : File = new File("tempfile")
     
     bucket.get.put(key, file)
   }
  
  /*
   * Reads ChannelInfo json from s3, decodes the json and returns a ChannelInfo
   * object. Parameters to the function are the channelName and version, to define
   * the s3 key
   */
  def readChannelData(name : String, version : Int): ChannelInfo = {
    val key : String = name + "/" + version
    val bucket: Option[Bucket] = s3.bucket(build("farmbot-dss-chanels"))
    
    val s3Object = bucket.get.getObject(key)
    var s3String : String = ""
    var i : Int = 1
    while (i != -1) {
      i = s3Object.get.getObjectContent().read()
      if (i != -1)
         s3String += i.asInstanceOf[Char]
    }
    
    val option : Option[ChannelInfo] = Parse.decodeOption[ChannelInfo](s3String)
    
    return option.get
  }
}