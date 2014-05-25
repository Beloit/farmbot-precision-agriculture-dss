package constants

import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2.AttributeType

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 4:47 PM
 * Contains constants relating to the ChannelFarm table
 * Specifically it contains all of the columns and their respective types, and the table name
 *
 * The ChannelFarm table contains an entry for each channel & farm combination
 */
object FarmChannelConstants {
  val TABLE_NAME: String = "ChannelFarm"

  val HASH_KEY: String = CHANNEL_AND_VERSION

  val CHANNEL_AND_VERSION: String = "ChannelAndVersion"
  val CHANNEL_AND_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val FARM_ID: String = "FarmID"
  val FARM_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val SCHEDULE: String = "Schedule"
  val SCHEDULE_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String
}
