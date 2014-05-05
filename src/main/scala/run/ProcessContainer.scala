package run

import scala.sys.process.{Process, ProcessIO}
import java.io._

class ProcessContainer(val executable: File, val input: Array[Byte], val finished: (File, File, Int) => Unit) {
  val outFile = File.createTempFile("dss-out", ".json")
  val errFile = File.createTempFile("dss-err", ".json")

  val outFileStream = new FileOutputStream(outFile)
  val errFileStream = new FileOutputStream(errFile)

  var runningProcess = Option.empty[Process]

  val io = new ProcessIO(
    in  => read(in),
    out => record(out, outFileStream),
    err => record(err, errFileStream))

  def record(input: InputStream, toWrite: OutputStream) = {
    val buf = Array.ofDim(1000)[Byte]

    var numRead: Int = -1
    do {
      numRead = input.read(buf)
      outFileStream.write(buf, 0, numRead)
    } while (numRead >= 0)

    input.close
  }

  def read(output: OutputStream) = {
    output.write(input)

    output.close()
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
    outFileStream.close

    errFileStream.flush
    errFileStream.close

    finished(outFile, errFile, exitCode)
  }
}
