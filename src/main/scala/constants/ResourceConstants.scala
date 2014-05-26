package constants

import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2.AttributeType

object ResourceConstants {
  val TABLE_NAME: String = "FarmResources"
    
  val FARM_CHANNEL_ID : String = "FarmChannelId"
  val FARM_CHANNEL_ID_TYPE : dynamodbv2.model.ScalarAttributeType = AttributeType.String
  
  val RESOURCE_ID : String = "ResourceId"
  val RESOURCE_ID_TYPE : dynamodbv2.model.ScalarAttributeType = AttributeType.String  
}