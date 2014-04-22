package helper

import awscala.{Credentials, CredentialsLoader}
import aws.AWSInitialization

trait RequiresAWS {
  assume(CredentialsLoader.load.isInstanceOf[Credentials])

  AWSInitialization.setup
}
