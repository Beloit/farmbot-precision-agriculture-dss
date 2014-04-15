package dynamo

import awscala.dynamodbv2.DynamoDB
import awscala.Region

/**
 * Created with IntelliJ IDEA.
 * User: cameron
 * Date: 4/14/14
 * Time: 5:17 PM
 * Anything that needs to access dynamo should use this so that we have a single instance of the accessor
 * You can extend it or if the class is already extending something else, use 'with'
 */
trait DynamoAccessor {
  implicit val dynamo = DynamoDB.at(Region.Oregon)
}
