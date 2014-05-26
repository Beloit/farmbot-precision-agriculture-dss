package s3

import types.{Module, JobInfo, ChannelInfo}
import java.io.File
import java.io.FileOutputStream
import awscala._
import awscala.s3._
import java.nio.file.{StandardCopyOption, Files}
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.github.fge.jsonschema.core.report.ProcessingReport
import aws.UsesPrefix

class RunDataAccessor extends S3Accessor with UsesPrefix {
  val channelInfoAccessor = new ChannelInfoAccessor

  // runDataType can be "in", "out", or "err"
  def writeRunData(info : JobInfo, runDataType : String, file : File) {
    println("in RunDataAccessor.writeRunData");
    val bucket: Option[Bucket] = s3.bucket(build("farmbot-dss-rundata"))

    val key: String = createKey(info, runDataType);
    println("key = " + key);
     
    bucket.get.put(key, file)
  }
   
  def writeRunData(info : JobInfo, runDataType : String, bytes : Array[Byte]) {
    println("in RunDataAccessor.writeRunData");
    val bucket: Option[Bucket] = s3.bucket(build("farmbot-dss-rundata"))

    val key: String = createKey(info, runDataType);
     
    val fos : FileOutputStream = new FileOutputStream("testfile")
     
    fos.write(bytes)
    fos.close()
     
    val file : File = new File("testfile")
     
    bucket.get.put(key, file)
  }
   
  private def createKey(info : JobInfo, runDataType : String) : String = {
    return info.farmId + "/" + info.jobId + "/" + runDataType;
  }

  def getRunData(info : JobInfo, runDataType : String): File = {
    val bucket: Option[Bucket] = s3.bucket(build("farmbot-dss-rundata"))

    val s3Obj = bucket.get.get(createKey(info, runDataType))

    if (s3Obj.isDefined) {
      val file = File.createTempFile("runData", ".dat")

      Files.copy(s3Obj.get.content, file.toPath, StandardCopyOption.REPLACE_EXISTING)

      return file
    }

    throw new RuntimeException("The run data requested was not found, jobInfo: " + info.toString + ", type:" + runDataType)
  }

  def findPreviousModule(job: JobInfo): Option[Module] = {
    val module = job.module

    val channelInfo = channelInfoAccessor.readChannelData(job.channel, job.channelVersion)

    var i = 0

    for (i <- 0 until channelInfo.modules.length) {
      if (channelInfo.modules(i) == module) {
        if (i > 0) {
          return Option.apply(channelInfo.modules(i - 1))
        } else {
          return Option.empty[Module]
        }
      }
    }

    throw new RuntimeException("The module was not found")
  }
  
  def isValid(outputFile : File, job : JobInfo) : Boolean = {
    val channelInfoAccessor : ChannelInfoAccessor = new ChannelInfoAccessor();
    val channelInfo : ChannelInfo = channelInfoAccessor.readChannelData(job.channel, job.channelVersion)
       
    val schema : JsonNode = JsonLoader.fromString(channelInfo.schema);
    val output : JsonNode = JsonLoader.fromFile(outputFile);
       
    val factory : JsonSchemaFactory = JsonSchemaFactory.byDefault();
    val channelSchema : JsonSchema = factory.getJsonSchema(schema);
       
    val report : ProcessingReport = channelSchema.validate(output)
    return report.isSuccess()
  }}

