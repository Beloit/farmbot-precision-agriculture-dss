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
  val BUCKET_NAME = "farmbot-dss-rundata"

  ensureBucketExists(build(BUCKET_NAME))

  val channelInfoAccessor = new ChannelInfoAccessor

  // runDataType can be "in", "out", or "err"
  def writeRunData(info : JobInfo, runDataType : String, file : File) {
    val bucket: Option[Bucket] = s3.bucket(build(BUCKET_NAME))

    val key: String = createKey(info.farmChannelId, info.jobId, runDataType)

    bucket.get.put(key, file)
  }
   
  def writeRunData(info : JobInfo, runDataType : String, bytes : Array[Byte]) {
    val bucket: Option[Bucket] = s3.bucket(build(BUCKET_NAME))

    val key: String = createKey(info.farmChannelId, info.jobId, runDataType)
     
    val fos : FileOutputStream = new FileOutputStream("testfile")
     
    fos.write(bytes)
    fos.close()
     
    val file : File = new File("testfile")
     
    bucket.get.put(key, file)
  }
   
  private def createKey(farmChannelId : String, jobId : String, runDataType : String) : String = {
    return farmChannelId + "/" + jobId + "/" + runDataType
  }

  def getRunData(farmChannelId : String, jobId : String, runDataType : String): File = {
    val bucket: Option[Bucket] = s3.bucket(build(BUCKET_NAME))

    val s3Obj = bucket.get.get(createKey(farmChannelId, jobId, runDataType))

    if (s3Obj.isDefined) {
      val file = File.createTempFile("runData", ".dat")

      Files.copy(s3Obj.get.content, file.toPath, StandardCopyOption.REPLACE_EXISTING)

      return file
    }

    throw new RuntimeException("The run data requested was not found, farmChannelId " + farmChannelId + "jobId: " + jobId + ", type:" + runDataType)
  }
  
  def isValid(outputFile : File, job : JobInfo) : Boolean = {
    val channelInfoAccessor : ChannelInfoAccessor = new ChannelInfoAccessor();
    val channelInfo : ChannelInfo = channelInfoAccessor.readChannelData(job.channel, job.channelVersion)
       
    val schema : JsonNode = JsonLoader.fromString(channelInfo.schema)

    val output : JsonNode = JsonLoader.fromFile(outputFile)
       
    val factory : JsonSchemaFactory = JsonSchemaFactory.byDefault()
    val channelSchema : JsonSchema = factory.getJsonSchema(schema)
       
    val report : ProcessingReport = channelSchema.validate(output)
    return report.isSuccess()
  }}

