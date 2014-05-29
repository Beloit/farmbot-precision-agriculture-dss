package run

import scala.collection.mutable.HashMap
import java.io.{InputStream, OutputStream, File}
import types.JobInfo

object ProcessHandler {
  private val persistentProcesses = new HashMap[String, ProcessContainer]
  private val instanceProcesses = new HashMap[String, Tuple2[ProcessContainer, Thread]]

  def startPersistentProcess(executable: File) = {
    executable.setExecutable(true, true)
    /*
    I can't decide the best way to divide input & output from the process
     */
  }

  def startInstanceProcess(job: JobInfo, executable: File, input: InputStream, timeout: Int, finished: (JobInfo, File, File, Int) => Unit) = {
    executable.setExecutable(true, true)

    val pc = new ProcessContainer(job, executable, input, finished)

    val stopThread = new Thread(new Runnable{
      def run = {
        Thread.sleep(timeout * 1000)
        pc.stop
      }
    })

    stopThread.start()

    instanceProcesses.put(executable.getName, (pc, stopThread))

    pc.start
  }
}