package aws.s3

import java.io.File
import constants.ModuleConstants
import s3.S3Accessor
import awscala.s3.S3Object
import java.nio.file.{StandardCopyOption, CopyOption, Files}

class ModuleAccessor extends S3Accessor {
  implicit val const = ModuleConstants

  val bucket = s3.bucket(const.BUCKET_NAME).get

  def getModuleExecutable(moduleName: String, moduleVersion: Int): Option[File] = {
    val key = const.key(moduleName, moduleVersion)

    val s3Obj: Option[S3Object] = bucket.get(key)

    if (s3Obj.isDefined) {
      val file = File.createTempFile("module", key)

      Files.copy(s3Obj.get.content, file.toPath, StandardCopyOption.REPLACE_EXISTING)

      return Option.apply(file)
    } else {
      return Option.empty
    }
  }

  def putModule(moduleName: String, moduleVersion: Int, module: File): String = {
    val key = const.key(moduleName, moduleVersion)

    return bucket.put(key, module).eTag
  }
}
