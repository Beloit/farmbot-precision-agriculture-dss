package run

import scala.collection.mutable.HashMap
import java.io.File

object ProcessHandler {
  private val persistentProcesses = new HashMap[String, ProcessContainer]
  private val instanceProcesses = new HashMap[String, Tuple2[ProcessContainer, Thread]]

  def startPersistentProcess(executable: File) = {
    executable.setExecutable(true, true)
    /*
    I can't decide the best way to divide input & output from the process
     */
  }

  def startInstanceProcess(executable: File, input: Array[Byte], timeout: Int, finished: (File, File, Int) => Unit) = {
    executable.setExecutable(true, true)

    val pc = new ProcessContainer(executable, input, finished)

    val stopThread = new Thread(new Runnable{
      def run = {
        Thread.sleep(timeout * 60 * 1000)
        pc.stop
      }
    })

    instanceProcesses.put(executable.getName, (pc, stopThread))

    pc.start
  }
}