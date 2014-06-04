package dynamo

import awscala._, dynamodbv2._
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import scala.collection.mutable
import types.FarmChannel
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.collection.mutable.ArrayBuffer
import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2
import scala.collection.immutable.Range

class FarmChannelAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = FarmChannelAccessor

  ensureChannelFarmTableExists

  val table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addEntry(farmChannel: FarmChannel) {
    table.putItem(farmChannel.key,
      const.FARM_ID -> farmChannel.farmID,
      const.SCHEDULE_HOUR -> farmChannel.scheduleHour.getOrElse(null),
      const.SCHEDULE_MINUTE -> farmChannel.scheduleMinute.getOrElse(null),
      const.LAST_RUN_TIME -> farmChannel.lastRunTime,
      const.OPAQUE_ID -> farmChannel.opaqueIdentifier)
  }

  def getFarmIdsForChannelVersion(channel: String, version: Int): Seq[String] = {
    val channelAndVersion = channel + "_" + version

    val cond = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(channelAndVersion))

    val items = dynamo.query(table, Seq(const.CHANNEL_AND_VERSION -> cond))

    return items.collect({case i: Item => i.attributes.find(_.name.contentEquals(const.FARM_ID)).get.value.s.get})
  }
  
  def getReadyJobs() : Seq[FarmChannel] = {
    // scan farm channel table for channels ready at the current time
    val today = Calendar.getInstance().getTime()
    val hourMinuteFormat = new SimpleDateFormat("h:mm")
    val curTimeStr = hourMinuteFormat.format(today)
    val currentMinute = curTimeStr.split(":")(1)
    val currentHour = curTimeStr.split(":")(0)
    val beginningOfHour = String.valueOf(System.currentTimeMillis() - (Integer.parseInt(currentMinute) * 60000))
    val beginningOfDay = String.valueOf(System.currentTimeMillis() - (Integer.parseInt(currentHour) * 3600000))
    
    val hourEmptyCond = new Condition().withComparisonOperator(ComparisonOperator.NULL);
    val hourNotEmptyCond = new Condition().withComparisonOperator(ComparisonOperator.NOT_NULL);
    val pastMinuteCondition = new Condition().withComparisonOperator(ComparisonOperator.LE).withAttributeValueList(new AttributeValue().withN(currentMinute))
    val curHourCondition = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withN(currentHour))
    val notRunYetHourlyCondition = new Condition().withComparisonOperator(ComparisonOperator.LT).withAttributeValueList(new AttributeValue().withN(beginningOfHour))
    val notRunYetDailyCondition = new Condition().withComparisonOperator(ComparisonOperator.LT).withAttributeValueList(new AttributeValue().withN(beginningOfDay))
    
    val hourlyItems = dynamo.scan(table, Seq(const.SCHEDULE_HOUR -> hourEmptyCond, const.SCHEDULE_MINUTE -> pastMinuteCondition, const.LAST_RUN_TIME -> notRunYetHourlyCondition))
    val dailyItems = dynamo.scan(table, Seq(const.SCHEDULE_HOUR -> hourNotEmptyCond, const.SCHEDULE_HOUR -> curHourCondition, const.SCHEDULE_MINUTE -> pastMinuteCondition, const.LAST_RUN_TIME -> notRunYetDailyCondition))

    val items = hourlyItems ++ dailyItems
    var readyChannels  = ArrayBuffer[FarmChannel]();

    println("readyJobs: " + items.size.toString)

    for (item <- items) {
      val channel : FarmChannel = new FarmChannel();
      
      for (attribute <- item.attributes) {
        val name = attribute.name
        val value = attribute.value
        
        if (name.equals(const.CHANNEL_AND_VERSION)) {
           channel.channel = value.getS.split("_")(0)
           channel.version = Integer.parseInt(value.getS.split("_")(1))
        } else if (name.equals(const.FARM_ID)) {
           channel.farmID = value.getS
        } else if (name.equals(const.SCHEDULE_HOUR)) {
          channel.scheduleHour = Option.apply(value.getN.toInt)
        } else if (name.equals(const.SCHEDULE_MINUTE)) {
          channel.scheduleMinute = Option.apply(value.getN.toInt)
        } else if (name.equals(const.OPAQUE_ID)) {
          channel.setOpaqueIdentifier(value.getS)
        }
      }

      readyChannels += channel
    }

    return readyChannels
  }

  private def ensureChannelFarmTableExists = {
    implicit val const = FarmChannelAccessor

    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.CHANNEL_AND_VERSION -> const.CHANNEL_AND_VERSION_TYPE,
        rangePK = const.FARM_ID -> const.FARM_ID_TYPE,
        otherAttributes = Seq(),
        indexes = Seq()
      )

      updateTableCapacity(tableName, 1, 1)
    }
  }
}


object FarmChannelAccessor {
  import com.amazonaws.services.dynamodbv2

  val TABLE_NAME: String = "ChannelFarm"

  val HASH_KEY: String = CHANNEL_AND_VERSION

  val CHANNEL_AND_VERSION: String = "ChannelAndVersion"
  val CHANNEL_AND_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val FARM_ID: String = "FarmID"
  val FARM_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val SCHEDULE_HOUR: String = "ScheduleHour"
  val SCHEDULE_HOUR_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val SCHEDULE_MINUTE: String = "ScheduleMinute"
  val SCHEDULE_MINUTE_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val LAST_RUN_TIME: String = "LastRunTime"
  val LAST_RUN_TIME_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val OPAQUE_ID: String = "OpaqueId"
  val OPAQUE_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String
}