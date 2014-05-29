package aws.s3

import java.io.File
import constants.ModuleConstants
import s3.S3Accessor
import awscala.s3.S3Object
import java.nio.file.{StandardCopyOption, CopyOption, Files}
import types.Module
import aws.UsesPrefix

class ModuleAccessor extends S3Accessor with UsesPrefix {
  implicit val const = ModuleConstants

  val bucket = s3.bucket(build(const.BUCKET_NAME)).get

  def getModuleExecutable(module: Module): Option[File] = {
    val s3Obj: Option[S3Object] = bucket.get(module.key)

    if (s3Obj.isDefined) {
      val file = File.createTempFile("module", module.key)

      Files.copy(s3Obj.get.content, file.toPath, StandardCopyOption.REPLACE_EXISTING)

      return Option.apply(file)
    } else {
      return Option.empty
    }
  }

  def putModule(module: Module, moduleExecutable: File): String = {
    return bucket.put(module.key, moduleExecutable).eTag
  }
}
