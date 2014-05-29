package aws.dynamo

import constants.ModuleConstants
import awscala.dynamodbv2.{Item, Table}
import dynamo.DynamoAccessor
import types.Module
import aws.UsesPrefix

class ModuleConfigurationAccessor extends DynamoAccessor with UsesPrefix  {
  implicit val const = ModuleConstants

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
}
