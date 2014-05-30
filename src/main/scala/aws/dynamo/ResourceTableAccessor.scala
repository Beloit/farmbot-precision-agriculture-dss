package dynamo

import aws.UsesPrefix
import constants.ResourceConstants
import awscala._, dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}


class ResourceTableAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = ResourceConstants

  ensureResourceTableExists

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get
  
  def getResources (farmChannelId : String) : Seq[String] = {
    val cond = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(farmChannelId))
    
    val items = dynamo.query(table, Seq(const.FARM_CHANNEL_ID -> cond))
  
    return items.collect({case i: Item => i.attributes.find(_.name.contentEquals(const.RESOURCE_ID)).get.value.s.get})
  }

  private def ensureResourceTableExists = {
    implicit val const = ResourceConstants

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.FARM_CHANNEL_ID -> const.FARM_CHANNEL_ID_TYPE,
        rangePK = const.RESOURCE_ID -> const.RESOURCE_ID_TYPE,
        otherAttributes = Seq(),
        indexes = Seq()
      )
    }
  }
}