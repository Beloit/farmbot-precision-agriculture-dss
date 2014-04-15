import awscala._, s3._
import dynamo.JobStatusAccessor
import types.JobInfo


/*
A great place for examples and us to fiddle around
Obviously this will go away once we're building the core not just all of the utilities
 */
object CameronPlayground extends App {
  implicit val s3 = S3()

  //val bucket: Bucket = s3.createBucket("farmbot-dss-test")
  //val bucket: Option[Bucket] = s3.bucket("farmbot-dss-test-that-doesn't exist")

  //println(bucket.isEmpty);
  AWSInitialization.setup("cameron-")

  val jsa: JobStatusAccessor = new JobStatusAccessor("cameron-")

  val ji : JobInfo = new JobInfo()
  ji.attempt = 0
  ji.channel = "TestChannel"
  ji.channelVersion = 1
  ji.farmId = "Somefarm"
  ji.jobId = 1234
  ji.module = "a module"
  ji.moduleVersion = 5

  jsa.addEntry(ji)

  println("Hello farm!")
}
