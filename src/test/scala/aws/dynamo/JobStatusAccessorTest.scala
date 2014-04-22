package dynamo

import dynamo.JobStatusAccessor
import org.scalatest.FlatSpec
import awscala.CredentialsLoader
import helper.RequiresAWS
import types.JobInfo

class JobStatusAccessorTest extends FlatSpec with RequiresAWS {
  "creating two identical jobs" should "have unique IDs" in {
    val ji : JobInfo = new JobInfo {
      attempt = 0
      channel = "TestChannel"
      channelVersion = 1
      farmId = "Somefarm"
      module = "a module"
      moduleVersion = 5
    }

    val jsa = new JobStatusAccessor

    val id1 = jsa.addEntry(ji)
    val id2 = jsa.addEntry(ji)

    assert(id1 != id2, "Job IDs are not unique")
  }
}
