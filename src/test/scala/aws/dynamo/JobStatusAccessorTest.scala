package dynamo

import org.scalatest.FlatSpec
import awscala.{Region, CredentialsLoader}
import helper.RequiresAWS
import types.{Module, JobInfo}
import awscala.dynamodbv2.{Table, DynamoDB}
import aws.UsesPrefix

class JobStatusAccessorTest extends FlatSpec with RequiresAWS with UsesPrefix {
  implicit val dynamo = DynamoDB.at(Region.Oregon)
  implicit val const = JobStatusAccessor

  val table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def withJob(testCode: String => Any) {
    val module = new Module{
      name = "a module"
      version = 5
    }

    val ji = new JobInfo {
      attempt = 0
      channel = "TestChannel"
      channelVersion = 1
      farmId = "Somefarm"
      resourceId = "someResource"
      module = module
    }

    val jsa = new JobStatusAccessor

    val id = jsa.addEntry(ji)

    try {
      testCode(id)
    }
    finally dynamo.deleteItem(table, id)
  }

  "creating two identical jobs" should "have unique IDs" in withJob { id1 =>
    withJob { id2 =>
      assert(id1 != id2, "Job IDs are not unique")
    }
  }

  "creating a job" must "show up in dynamo" in withJob { id =>
    assert(dynamo.get(table, id).isDefined)
  }


}
