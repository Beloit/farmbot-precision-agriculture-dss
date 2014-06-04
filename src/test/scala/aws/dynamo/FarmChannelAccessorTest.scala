package dynamo

import org.scalatest.FlatSpec
import awscala.Region
import helper.RequiresAWS
import awscala.dynamodbv2.{Table, DynamoDB}
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import types.FarmChannel
import java.util.Calendar
import java.text.SimpleDateFormat

class FarmChannelAccessorTest extends FlatSpec with RequiresAWS with UsesPrefix {
  implicit val dynamo = DynamoDB.at(Region.Oregon)
  implicit val const = FarmChannelAccessor
  val SIX_MINUTES = 360000
  
  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  val accessor = new FarmChannelAccessor

  "multipe farms" can "exist under one farm_channel" in {
    //accessor.addEntry("testChannel", 1, "farm1")
    //accessor.addEntry("testChannel", 1, "farm2")

    val ids = accessor.getFarmIdsForChannelVersion("testChannel", 1)

    assert(ids.contains("farm1"))
    assert(ids.contains("farm2"))
  }
  
  "getReadyJobs" should "should return farm channels that are ready based on scheduling" in {
    var farmChannel : FarmChannel = new FarmChannel {
      channel = "channel_"
      version = 1
      farmID = "farmId"
    }
     
    val today = Calendar.getInstance().getTime()
    val minuteFormat = new SimpleDateFormat("mm")
    val hourFormat = new SimpleDateFormat("h")
    val currentMinute = minuteFormat.format(today).toInt
    val currentHour = hourFormat.format(today).toInt

    var a : Int = 0
    for (a <- 1 to 5) {
      farmChannel.channel = "channel" + a
      farmChannel.scheduleHour = Option.apply(currentHour)
      farmChannel.scheduleMinute = Option.apply(currentMinute - 5)
      
      accessor.addEntry(farmChannel);
    }
    
    var readyChannels : List[String] = List("channel1", "channel2", "channel3", "channel4", "channel5")
    
    for (a <- 6 to 10) {
      farmChannel.channel = "channel" + a
      farmChannel.scheduleHour = Option.apply(currentHour)
      farmChannel.scheduleMinute = Option.apply(currentMinute + 5)
      
      accessor.addEntry(farmChannel)
    }
    
    var readyJobs : Seq[FarmChannel] = accessor.getReadyJobs
    
    assert(readyJobs.size == 5)
    
    var x : Int = 1
    for (farmChannel <- readyJobs) {
      assert(readyChannels.contains(farmChannel.channel))
    }
    
    Thread.sleep(SIX_MINUTES)
    
    readyJobs = accessor.getReadyJobs
    assert(readyJobs.size == 10)
    
    readyChannels ++ List("channel6", "channel7", "channel8", "channel9", "channel10")
    
    x = 1
    for (farmChannel <- readyJobs) {
      assert(readyChannels.contains(farmChannel.channel)) 
    }
  }
}
