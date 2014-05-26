package dynamo

import aws.UsesPrefix
import constants.ResourceConstants
import awscala._, dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}


class ResourceTableAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = ResourceConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get
  
  def getResources (farmChannelId : String) : Seq[String] = {
    val cond = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(farmChannelId))
    
    val items = dynamo.query(table, Seq(const.FARM_CHANNEL_ID -> cond))
  
    return items.collect({case i: Item => i.attributes.find(_.name.contentEquals(const.RESOURCE_ID)).get.value.s.get})
  }
}