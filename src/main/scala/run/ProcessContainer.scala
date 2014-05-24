package run

import scala.sys.process.{Process, ProcessIO}
import java.io._
import org.apache.commons.io.IOUtils
import types.JobInfo

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
    runningProcess = Option.apply(Process(executable.getAbsolutePath).run(io))
    new Thread(new Runnable {
      def run = {
        finish(runningProcess.get.exitValue)
      }
    })
  }

  def stop = {
    if (runningProcess.isDefined) {
      runningProcess.get.destroy
      runningProcess = Option.empty[Process]
    }
  }

  def finish(exitCode: Int) = {
    outFileStream.flush
    IOUtils.closeQuietly(outFileStream)

    errFileStream.flush
    IOUtils.closeQuietly(errFileStream)

    finished(job, outFile, errFile, exitCode)
  }
}
