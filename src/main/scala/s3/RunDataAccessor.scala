package s3

import types.JobInfo
import java.io.File
import java.io.FileOutputStream
import awscala._
import awscala.s3._

class RunDataAccessor extends S3Accessor {
  // runDataType can be "in", "out", or "err"
   def writeRunData(info : JobInfo, runDataType : String, file : File) {
     println("in RunDataAccessor.writeRunData");
     val bucket: Option[Bucket] = s3.bucket("cameron-farmbot-dss-rundata")
     
     val key: String = createKey(info, runDataType);
     println("key = " + key);
     
     bucket.get.put(key, file)  
   }
   
   def writeRunData(info : JobInfo, runDataType : String, bytes : Array[Byte]) {
     println("in RunDataAccessor.writeRunData");
     val bucket: Option[Bucket] = s3.bucket("farmbot-dss-rundata")
     
     val key: String = createKey(info, runDataType);
     
     val fos : FileOutputStream = new FileOutputStream("testfile")
     
     fos.write(bytes)
     fos.close()
     
     val file : File = new File("testfile")
     
     bucket.get.put(key, file)  
   }
   
   private def createKey(info : JobInfo, runDataType : String) : String = {
     println("moduleVersion in create key = " + info.moduleVersion)
     
     return info.farmId + "/" + info.jobId + "/" + info.channel + "_" + info.channelVersion + "/" + info.module + "_" + info.moduleVersion + "/" + runDataType + "_" + info.attempt;
   }
}