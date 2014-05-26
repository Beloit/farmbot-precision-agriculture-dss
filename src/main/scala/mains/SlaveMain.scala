package mains

import dynamo.JobStatusAccessor
import run.ExecuteModule

object SlaveMain extends App {
  val jobStatusAccessor = new JobStatusAccessor
  val executeModule = new ExecuteModule

  val ONE_SECOND = 1000

  override def main(args: Array[String]) = {
    while (true) {
      val availableJob = jobStatusAccessor.findReadyJob

      if (availableJob.isDefined) {
        executeModule.run(availableJob.get)
      } else {
        Thread.sleep(10 * ONE_SECOND)
      }
    }

  }
}
