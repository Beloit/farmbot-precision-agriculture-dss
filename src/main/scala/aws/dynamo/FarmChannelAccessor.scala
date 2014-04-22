package dynamo

import awscala._, dynamodbv2._
import constants.FarmChannelConstants
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import scala.collection.mutable

class FarmChannelAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = FarmChannelConstants

  val table: Table = dynamo.table(prefix + const.TABLE_NAME).get

  def addEntry(channel: String, version: Int, farmId: String) {
    val channelAndVersion = channel + "_" + version

    table.putItem(channelAndVersion,
      const.FARM_ID -> farmId)
  }

  def getFarmIdsForChannelVersion(channel: String, version: Int): Seq[String] = {
    val channelAndVersion = channel + "_" + version

    val cond = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(channelAndVersion))

    val items = dynamo.query(table, Seq(const.CHANNEL_AND_VERSION -> cond))

    return items.collect({case i: Item => i.attributes.find(_.name.contentEquals(const.FARM_ID)).get.value.s.get})
  }

}
