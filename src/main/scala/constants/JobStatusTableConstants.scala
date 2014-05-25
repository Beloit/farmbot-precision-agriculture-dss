package constants

import awscala.dynamodbv2.AttributeType
import com.amazonaws.services.dynamodbv2

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 4:37 PM
 * Contains constants relating to the JobStatusTable
 * Specifically it contains all of the columns and their respective types, and the table name
 *
 * The JobStatusTable contains entries for each module for a particular farm or channel
 * This is used to keep track of the status of jobs
 */
object JobStatusTableConstants {
  val TABLE_NAME: String = "JobStatus"

  val HASH_KEY: String = FARM_CHANNEL_ID
  val RANGE_KEY: String = JOB_ID

  val JOB_ID: String = "JobId"
  val JOB_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val FARM_CHANNEL_ID: String = "FarmChannelId"
  val FARM_CHANNEL_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val ADDED_AT: String = "AddedAt"
  val ADDED_AT_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val FARM_ID: String = "FarmID"
  val FARM_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val RESOURCE_ID: String = "ResourceID"
  val RESOURCE_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val CHANNEL: String = "Channel"
  val CHANNEL_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val CHANNEL_VERSION: String = "ChannelVersion"
  val CHANNEL_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val MODULE: String = "Module"
  val MODULE_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val MODULE_VERSION: String = "ModuleVersion"
  val MODULE_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val STATUS: String = "Status"
  val STATUS_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val LAST_STATUS_CHANGE: String = "LastStatusChange"
  val LAST_STATUS_CHANGE_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val NEXT_ID: String = "NextId"
  val NEXT_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val PREVIOUS_ID: String = "PreviousId"
  val PREVIOUS_ID_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  object JobStatus extends Enumeration {
    type JobStatus = Value

    val Pending, Ready, Running, NoRetryError, Success, ErrorInvalidOutput, RetriesExceeded = Value
  }
}