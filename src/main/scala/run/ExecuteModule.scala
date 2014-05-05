package run

import dynamo.JobStatusAccessor
import types.JobInfo
import constants.JobStatusTableConstants.JobStatus
import aws.dynamo.ModuleConfigurationAccessor
import aws.s3.ModuleAccessor
import java.io.File

class ExecuteModule {
  val jobStatusAccessor: JobStatusAccessor = new JobStatusAccessor
  val moduleConfAccessor: ModuleConfigurationAccessor = new ModuleConfigurationAccessor
  val moduleAccessor = new ModuleAccessor

  def run(job: JobInfo, inputKey: String) = {
    /*if (moduleConfAccessor.isModulePersistent(job.module, job.moduleVersion)) {

    } else {

    }*/
    val executableFile = moduleAccessor.getModuleExecutable(job.module, job.moduleVersion)

    if (executableFile.isDefined) {
      //Get input file from S3!!!

      ProcessHandler.startInstanceProcess(executableFile.get, Array.ofDim(100)[Byte], 10, moduleFinished)

      jobStatusAccessor.updateStatus(job.jobId, JobStatus.Running)

    }



  }

  def moduleFinished(stdout: File, stderr: File, exitCode: Int) = {
    //Write outputs to S3 and verify that they are valid
  }


}
