package dynamo

import awscala._, dynamodbv2._
import constants.FarmChannelConstants
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import scala.collection.mutable
import types.FarmChannel
import java.util.Calendar
import java.text.SimpleDateFormat

class FarmChannelAccessor extends DynamoAccessor with UsesPrefix {
  implicit val const = FarmChannelConstants

  val table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addEntry(farmChannel: FarmChannel) {
    table.putItem(farmChannel.key,
      const.FARM_ID -> farmChannel.farmID,
      const.SCHEDULE_HOUR -> farmChannel.scheduleHour,
      const.SCHEDULE_MINUTE -> farmChannel.scheduleMinute)
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
    
    val hourEmptyCond = new Condition().withComparisonOperator(ComparisonOperator.NULL);
    val hourNotEmptyCond = new Condition().withComparisonOperator(ComparisonOperator.NOT_NULL);
    val pastMinuteCondition = new Condition().withComparisonOperator(ComparisonOperator.LE).withAttributeValueList(new AttributeValue().withS(currentMinute))
    val curHourCondition = new Condition().withComparisonOperator(ComparisonOperator.EQ).withAttributeValueList(new AttributeValue().withS(currentHour))
    
    val hourlyItems = dynamo.scan(table, Seq(const.SCHEDULE_HOUR -> hourEmptyCond, const.SCHEDULE_MINUTE -> pastMinuteCondition))
    val dailyItems = dynamo.scan(table, Seq(const.SCHEDULE_HOUR -> hourNotEmptyCond, const.SCHEDULE_HOUR -> curHourCondition, const.SCHEDULE_MINUTE -> pastMinuteCondition))
    
    val items = hourlyItems ++ dailyItems
    var readyChannels : Seq[FarmChannel] = Seq[FarmChannel]();
    
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
          channel.scheduleHour = value.getS
        } else if (name.equals(const.SCHEDULE_MINUTE)) {
          channel.scheduleMinute = value.getS
        }
      }
      
      readyChannels :+ channel
    }
    
    return readyChannels
  }

}
