package dynamo

import aws.UsesPrefix
import awscala._, dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import types.FarmChannel
import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2


class ResourceTableAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = ResourceTableAccessor

  ensureResourceTableExists

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get
  
  def getResources(farmChannel : FarmChannel) : Seq[String] = {
    val cond = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(farmChannel.opaqueIdentifier))
    
    val items = dynamo.query(table, Seq(const.FARM_CHANNEL_ID -> cond))
  
    return items.collect({case i: Item => i.attributes.find(_.name.contentEquals(const.RESOURCE_ID)).get.value.s.get})
  }

  private def ensureResourceTableExists = {
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

      updateTableCapacity(tableName, 1, 1)
    }
  }
}

object ResourceTableAccessor {
  import com.amazonaws.services.dynamodbv2

  val TABLE_NAME: String = "FarmResources"

  val FARM_CHANNEL_ID : String = "FarmChannelId"
  val FARM_CHANNEL_ID_TYPE : dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val RESOURCE_ID : String = "ResourceId"
  val RESOURCE_ID_TYPE : dynamodbv2.model.ScalarAttributeType = AttributeType.String
}