package run

import dynamo.JobStatusAccessor
import s3.{ChannelInfoAccessor, RunDataAccessor}
import types.{Module, JobInfo}
import constants.JobStatusTableConstants.JobStatus
import aws.dynamo.ModuleConfigurationAccessor
import aws.s3.ModuleAccessor
import java.io.{FileOutputStream, FileInputStream, File}
import org.apache.commons.io.IOUtils

class ExecuteModule {
  val jobStatusAccessor: JobStatusAccessor = new JobStatusAccessor
  val moduleConfAccessor: ModuleConfigurationAccessor = new ModuleConfigurationAccessor
  val moduleAccessor = new ModuleAccessor
  val runDataAccessor = new RunDataAccessor

  def run(job: JobInfo) = {
    if (false /*moduleConfAccessor.isModulePersistent(job.module, job.moduleVersion)*/) {

    } else {
      val executableFile = moduleAccessor.getModuleExecutable(job.module)

      if (executableFile.isDefined) {
        val previousModule = runDataAccessor.findPreviousModule(job);

        val previousJob = job.copy
        previousJob.module = previousModule

        val previousOutput: Option[File] = runDataAccessor.getRunData(previousJob, "out")

        if (previousOutput.isDefined) {
          jobStatusAccessor.updateStatus(job.jobId, JobStatus.Running)

          val inStream = new FileInputStream(previousOutput.get)

          ProcessHandler.startInstanceProcess(job, executableFile.get, inStream, 10, moduleFinished)
        }

      }
    }
  }

  def moduleFinished(job: JobInfo, stdout: File, stderr: File, exitCode: Int) = {
    if (exitCode == 0) {
      if (true /*valid output*/) {
        runDataAccessor.writeRunData(job, "out", stdout)

        jobStatusAccessor.updateStatus(job.jobId, JobStatus.Success)
      } else {
        val errFileStream = new FileOutputStream(stderr)
        val outFileStream = new FileInputStream(stdout)

        IOUtils.write("\nOutput was invalid, attaching output below:\n", errFileStream)

        IOUtils.copy(outFileStream, errFileStream)

        IOUtils.closeQuietly(outFileStream)
        IOUtils.closeQuietly(errFileStream)

        jobStatusAccessor.updateStatus(job.jobId, JobStatus.ErrorInvalidOutput)
      }
    } else {
      jobStatusAccessor.updateStatus(job.jobId, JobStatus.Error)
    }

    runDataAccessor.writeRunData(job, "err", stderr)
  }


}
