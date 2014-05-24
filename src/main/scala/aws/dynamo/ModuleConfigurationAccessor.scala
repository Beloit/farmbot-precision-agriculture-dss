package aws.dynamo

import constants.ModuleConstants
import awscala.dynamodbv2.{Item, Table}
import dynamo.DynamoAccessor
import types.{Module, JobInfo}
import awscala.DateTime
import constants.JobStatusTableConstants.JobStatus
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants.JobStatus._
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class ModuleConfigurationAccessor extends DynamoAccessor with UsesPrefix  {
  implicit val const = ModuleConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addModule(module: Module, persistent: Boolean, timeout: Int) = {
    var persistentS = "NO"

    if (persistent) {
      persistentS = "YES"
    }

    table.putItem(module.key,
      const.PERSISTENT -> persistentS,
      const.TIMEOUT -> timeout)
  }

  def isModulePersistent(module: Module): Boolean = {
    "yes" equalsIgnoreCase table.get(module.key).get.attributes.collectFirst({case i: Item => i.attributes.find(_.name.contentEquals(const.PERSISTENT)).get.value.s.get}).get
  }

  def getModuleTimeout(module: Module): Int = {
    table.get(module.key).get.attributes.collectFirst({case i: Item => i.attributes.find(_.name.contentEquals(const.TIMEOUT)).get.value.n.get}).get.toInt
  }

  def populateModuleConfiguration(module: Module) = {
    table.get(module.key).get.attributes.collectFirst({case i: Item =>
      module.timeout = i.attributes.find(_.name.contentEquals(const.TIMEOUT)).get.value.n.get.toInt
      module.persistent = i.attributes.find(_.name.contentEquals(const.PERSISTENT)).get.value.s.get equalsIgnoreCase("yes")})
  }
}
