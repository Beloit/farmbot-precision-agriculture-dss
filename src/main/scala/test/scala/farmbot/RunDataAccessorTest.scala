package src.test.scala.s3

import s3.RunDataAccessor
import types.JobInfo
import java.io.File
import java.io.FileOutputStream
import scala.Byte
import farmbot.AWSInitialization

object RunDataAccessorTest {
  def main(args: Array[String]) = {
    println("in runDataAccessorTest");
    val rda : RunDataAccessor = new RunDataAccessor()
    var jobInfo : JobInfo = new JobInfo("farmId", 1, "channel", 1, "module", 1, 1)

    /* val fos : FileOutputStream = new FileOutputStream("testfile")
    fos.write("this is a run data test file!!!".toCharArray().map(_.toByte))
    fos.close() */
  
    rda.writeRunData(jobInfo, "in", new File("testfile"))  
  }
}