package helper

import awscala.{Credentials, CredentialsLoader}

trait RequiresAWS {
  assume(CredentialsLoader.load.isInstanceOf[Credentials])
}
