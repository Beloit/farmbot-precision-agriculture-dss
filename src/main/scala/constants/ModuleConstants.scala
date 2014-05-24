package constants

import com.amazonaws.services.dynamodbv2
import awscala.dynamodbv2.AttributeType

object ModuleConstants {
  val TABLE_NAME: String = "ModuleConfiguration"

  val HASH_KEY: String = MODULE_NAME_VERSION

  val MODULE_NAME_VERSION: String = "ModuleName"
  val MODULE_NAME_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val PERSISTENT: String = "Persistent"
  val PERSISTENT_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val BUCKET_NAME: String = "modules"
}
