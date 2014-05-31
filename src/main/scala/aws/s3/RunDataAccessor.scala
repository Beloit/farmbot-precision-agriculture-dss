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
  implicit val const = RunDataAccessor

  val channelInfoAccessor = new ChannelInfoAccessor

  // runDataType can be "in", "out", or "err"
  def writeRunData(info : JobInfo, runDataType : String, file : File) {
    val key: String = createKey(info.farmChannelId, info.jobId, runDataType)

    const.bucket.put(key, file)
  }
   
  def writeRunData(info : JobInfo, runDataType : String, bytes : Array[Byte]) {
    val key: String = createKey(info.farmChannelId, info.jobId, runDataType)
     
    val fos : FileOutputStream = new FileOutputStream("testfile")
     
    fos.write(bytes)
    fos.close()
     
    val file : File = new File("testfile")

    const.bucket.put(key, file)
  }
   
  private def createKey(farmChannelId : String, jobId : String, runDataType : String) : String = {
    return farmChannelId + "/" + jobId + "/" + runDataType
  }

  def getRunData(farmChannelId : String, jobId : String, runDataType : String): File = {
    val s3Obj = const.bucket.get(createKey(farmChannelId, jobId, runDataType))

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
  }
}

object RunDataAccessor extends S3Accessor with UsesPrefix{
  val BUCKET_NAME = "rundata"

  def bucketName = build(BUCKET_NAME)

  ensureBucketExists(bucketName)

  def bucket = s3.bucket(bucketName).get
}

