package run

import scala.sys.process.{Process, ProcessIO}
import java.io._
import org.apache.commons.io.IOUtils
import types.JobInfo
import constants.ModuleConstants

class ProcessContainer(val job: JobInfo, val executable: File, val input: InputStream, val finished: (JobInfo, File, File, Int) => Unit) {
  val outFile = File.createTempFile("dss-out", ".json")
  val errFile = File.createTempFile("dss-err", ".json")

  val outFileStream = new FileOutputStream(outFile)
  val errFileStream = new FileOutputStream(errFile)

  var runningProcess = Option.empty[Process]

  val io = new ProcessIO(
    in  => read(in),
    out => record(out, outFileStream),
    err => record(err, errFileStream))

  def record(input: InputStream, output: OutputStream) = {
    IOUtils.copy(input, output)
    IOUtils.closeQuietly(input)
  }

  def read(output: OutputStream) = {
    IOUtils.copy(input, output)
    IOUtils.closeQuietly(output)
  }

  def start = {
    val args = List("-farmID", job.farmId, "-resourceID", job.resourceId)

    Process.apply("chmod 777 " + executable.getAbsolutePath).run.exitValue

    runningProcess = Option.apply(Process.apply(executable.getAbsolutePath, args).run(io))
    println("started!")
    new Thread(new Runnable {
      def run = {
        println("from inside the thread?")
        finish(runningProcess.get.exitValue)
      }
    }).start
  }

  def stop = {
    println("stop!")
    if (runningProcess.isDefined) {
      runningProcess.get.destroy
      runningProcess = Option.empty[Process]

      finish(ModuleConstants.TIMEOUT_CODE)
    }
  }

  def finish(exitCode: Int) = {
    println("finish!")
    outFileStream.flush
    IOUtils.closeQuietly(outFileStream)

    errFileStream.flush
    IOUtils.closeQuietly(errFileStream)

    finished(job, outFile, errFile, exitCode)
  }
}
