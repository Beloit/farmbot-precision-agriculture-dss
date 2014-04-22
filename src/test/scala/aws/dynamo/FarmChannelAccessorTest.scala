package dynamo

import org.scalatest.FlatSpec
import awscala.Region
import helper.RequiresAWS
import awscala.dynamodbv2.{Table, DynamoDB}
import aws.UsesPrefix
import constants.FarmChannelConstants
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}

class FarmChannelAccessorTest extends FlatSpec with RequiresAWS with UsesPrefix {
  implicit val dynamo = DynamoDB.at(Region.Oregon)
  implicit val const = FarmChannelConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  val accessor = new FarmChannelAccessor

  "multipe farms" can "exist under one farm_channel" in {
    accessor.addEntry("testChannel", 1, "farm1")
    accessor.addEntry("testChannel", 1, "farm2")

    val ids = accessor.getFarmIdsForChannelVersion("testChannel", 1)

    assert(ids.contains("farm1"))
    assert(ids.contains("farm2"))
  }
}
