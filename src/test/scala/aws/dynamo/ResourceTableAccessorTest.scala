package aws.dynamo

import org.scalatest.FlatSpec
import awscala.Region
import helper.RequiresAWS
import awscala.dynamodbv2.{Table, DynamoDB}
import aws.UsesPrefix
import constants.FarmChannelConstants
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, ComparisonOperator, Condition}
import dynamo.ResourceTableAccessor
import constants.ResourceConstants

class ResourceTableAccessorTest extends FlatSpec with RequiresAWS with UsesPrefix {
  val FARM_CHANNEL_ID = "farmChannelId"
  val OTHER_FARM_CHANNEL_ID = "otherFarmChannelId"
  val RESOURCE_ID = "resourceId_"
  val OTHER_RESOURCE_ID = "otherResourceId_"
    
  implicit val dynamo = DynamoDB.at(Region.Oregon)
  implicit val const = ResourceConstants
   
  val accessor = new ResourceTableAccessor
  var table: Table = dynamo.table(build(const.TABLE_NAME)).get
    
  "getEntries(farmChannelId)" should "return list of resourceIds in table for that farmChannelId" in {
    // println("adding entries...")
    
    var a : Int = 0
    for (a <- 1 to 5) {
      accessor.addEntry(FARM_CHANNEL_ID, RESOURCE_ID + a)
    }
    
    for (a <- 1 to 3) {
      accessor.addEntry(OTHER_FARM_CHANNEL_ID, OTHER_RESOURCE_ID + a) 
    }
    
    var resources : Seq[String] = accessor.getResources(FARM_CHANNEL_ID)
    var otherResources : Seq[String] = accessor.getResources(OTHER_FARM_CHANNEL_ID)
    
    assert(resources.size == 5)
    assert(otherResources.size == 3)
    
    for (a <- 1 to 5) { 
      assert(resources.contains(RESOURCE_ID + a))
      assert(!resources.contains(OTHER_RESOURCE_ID + a))
    }
    
    for (a <- 1 to 3) {
      assert(otherResources.contains(OTHER_RESOURCE_ID + a))
      assert(!otherResources.contains(RESOURCE_ID + a))
    }
  }
}