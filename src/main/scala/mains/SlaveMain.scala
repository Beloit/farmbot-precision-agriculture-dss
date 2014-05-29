package mains

import dynamo.JobStatusAccessor
import run.ExecuteModule
import aws.AWSInitialization
import scala.Predef._

object SlaveMain extends App {
  AWSInitialization.setup

  val executeModule = new ExecuteModule
  val jobStatusAccessor = new JobStatusAccessor

  val ONE_SECOND : Int = 1000

  val availableJob = jobStatusAccessor.findReadyJob

  forever(
    if (availableJob.isDefined) {
      executeModule.run(availableJob.get)
    } else {
      Thread.sleep(10 * ONE_SECOND)
    }
  )

  def forever[A](body: => A): Nothing = {
    body
    println("yayay")
    forever(body)
  }
}
