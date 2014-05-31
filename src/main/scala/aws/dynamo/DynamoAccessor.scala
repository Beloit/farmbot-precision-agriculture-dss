package dynamo

import awscala.dynamodbv2.{ProvisionedThroughput, DynamoDB}
import awscala.Region

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 5:17 PM
 * Anything that needs to access aws.dynamo should use this so that we have a single instance of the accessor
 * You can extend it or if the class is already extending something else, use 'with'
 */
trait DynamoAccessor {
  implicit val dynamo = DynamoDB.at(Region.Oregon)

  def updateTableCapacity(tableName: String, readCap: Long, writeCap: Long) = {
    untilTrue(tryUpdate(tableName, readCap, writeCap))
  }

  private def tryUpdate(tableName: String, readCap: Long, writeCap: Long): Boolean = {
    Thread.sleep(1000)
    try {
      dynamo.table(tableName).get.update(ProvisionedThroughput(1L, 1L))
    } catch {
      case e: Exception => {
        return false
      }
    }
    return true
  }

  private def untilTrue[Boolean](body: => Boolean): Unit = {
    val test: Boolean = body

    if (test == false) {
      untilTrue(body)
    }
  }
}
