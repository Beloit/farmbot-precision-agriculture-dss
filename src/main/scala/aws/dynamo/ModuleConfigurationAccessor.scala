package aws.dynamo

import awscala.dynamodbv2.{AttributeType, ProvisionedThroughput, Item, Table}
import dynamo.DynamoAccessor
import types.Module
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2

class ModuleConfigurationAccessor extends DynamoAccessor with UsesPrefix  {
  implicit val const = ModuleConfigurationAccessor

  ensureModuleConfigurationTableExists

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addModule(module: Module, persistent: Boolean, timeoutSeconds: Int) = {
    var persistentS = "NO"

    if (persistent) {
      persistentS = "YES"
    }

    table.putItem(module.key,
      const.PERSISTENT -> persistentS,
      const.TIMEOUT -> timeoutSeconds)
  }

  def isModulePersistent(module: Module): Boolean = {
    "yes" equalsIgnoreCase table.get(module.key).get.attributes.collectFirst({case i: Item => i.attributes.find(_.name.contentEquals(const.PERSISTENT)).get.value.s.get}).get
  }

  def getModuleTimeout(module: Module): Int = {
    table.get(module.key).get.attributes.collectFirst({case i: Item => i.attributes.find(_.name.contentEquals(const.TIMEOUT)).get.value.n.get}).get.toInt
  }

  def populateModuleConfiguration(module: Module) = {
    println("isDefined: " + table.get(module.key).isDefined.toString)

    table.get(module.key).get.attributes.collectFirst({case i: Item =>
      module.timeout = i.attributes.find(_.name.contentEquals(const.TIMEOUT)).get.value.n.get.toInt
      module.persistent = i.attributes.find(_.name.contentEquals(const.PERSISTENT)).get.value.s.get equalsIgnoreCase("yes")})
  }

  private def ensureModuleConfigurationTableExists = {
    val tableName: String = build(const.TABLE_NAME)
    val table: Option[Table] = dynamo.table(tableName)

    if (table.isEmpty) {
      dynamo.createTable(
        name = tableName,
        hashPK = const.MODULE_NAME_VERSION -> const.MODULE_NAME_VERSION_TYPE
      )

      updateTableCapacity(tableName, 1, 1)
    }
  }
}

object ModuleConfigurationAccessor {
  val TABLE_NAME: String = "ModuleConfiguration"

  val HASH_KEY: String = MODULE_NAME_VERSION

  val MODULE_NAME_VERSION: String = "ModuleName"
  val MODULE_NAME_VERSION_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val PERSISTENT: String = "Persistent"
  val PERSISTENT_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.String

  val TIMEOUT: String = "Timeout"
  val TIMEOUT_TYPE: dynamodbv2.model.ScalarAttributeType = AttributeType.Number

  val SUCCESS_CODE : Int = 0
  val RETRYABLE_ERROR_CODE : Int = 1
  val TIMEOUT_CODE : Int = -1
}
