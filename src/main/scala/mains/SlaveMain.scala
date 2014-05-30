package mains

import dynamo.JobStatusAccessor
import run.ExecuteModule
import scala.Predef._

object SlaveMain extends App {
  val executeModule = new ExecuteModule
  val jobStatusAccessor = new JobStatusAccessor

  val ONE_SECOND : Int = 1000

  forever({
    val availableJob = jobStatusAccessor.findReadyJob

    if (availableJob.isDefined) {
      println("gonna run: " + availableJob.get.jobId)
      executeModule.run(availableJob.get)
    } else {
      Thread.sleep(10 * ONE_SECOND)
    }
  })

  def forever[A](body: => A): Nothing = {
    body

    forever(body)
  }
}
