package aws.dynamo

import constants.ModuleConstants
import awscala.dynamodbv2.{Item, Table}
import dynamo.DynamoAccessor
import types.JobInfo
import awscala.DateTime
import constants.JobStatusTableConstants.JobStatus
import org.joda.time.format.ISODateTimeFormat
import constants.JobStatusTableConstants.JobStatus._
import aws.UsesPrefix
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class ModuleConfigurationAccessor extends DynamoAccessor with UsesPrefix  {
  implicit val const = ModuleConstants

  var table: Table = dynamo.table(build(const.TABLE_NAME)).get

  def addModule(name: String, version: Int, persistent: Boolean) = {
    var persistentS = "NO"

    if (persistent) {
      persistentS = "YES"
    }

    table.putItem(const.key(name, version),
      const.PERSISTENT -> persistentS)
  }

  def isModulePersistent(name: String, version: Int): Boolean = {
    "yes" equalsIgnoreCase table.get(const.key(name, version)).get.attributes.collectFirst({case i: Item => i.attributes.find(_.name.contentEquals(const.PERSISTENT)).get.value.s.get}).get
  }
}
