package src.test.scala.s3

import s3.RunDataAccessor
import types.{Module, JobInfo}
import java.io.File
import org.joda.time.DateTime

object RunDataAccessorTest {
  def main(args: Array[String]) = {
    println("in runDataAccessorTest");
    val rda : RunDataAccessor = new RunDataAccessor()
    val module = new Module
    module.name = "module"
    module.version = 1

    val jobInfo : JobInfo = new JobInfo
    jobInfo.farmId = "farmId"
    jobInfo.channel = "channel"
    jobInfo.channelVersion = 1
    jobInfo.module = module
    jobInfo.attempt = 1

    /* val fos : FileOutputStream = new FileOutputStream("testfile")
    fos.write("this is a run data test file!!!".toCharArray().map(_.toByte))
    fos.close() */
  
    rda.writeRunData(jobInfo, "in", new File("testfile"))  
  }
}