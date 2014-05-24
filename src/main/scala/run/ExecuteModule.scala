package run

import dynamo.JobStatusAccessor
import s3.{ChannelInfoAccessor, RunDataAccessor}
import types.{Module, JobInfo}
import constants.JobStatusTableConstants.JobStatus
import aws.dynamo.ModuleConfigurationAccessor
import aws.s3.ModuleAccessor
import java.io._
import org.apache.commons.io.IOUtils
import constants.ModuleConstants

class ExecuteModule {
  val MAX_RETRIES: Int = 3

  implicit val const = ModuleConstants

  val jobStatusAccessor: JobStatusAccessor = new JobStatusAccessor
  val moduleConfAccessor: ModuleConfigurationAccessor = new ModuleConfigurationAccessor
  val moduleAccessor = new ModuleAccessor
  val runDataAccessor = new RunDataAccessor
  val channelInfoAcessor = new ChannelInfoAccessor

  def run(job: JobInfo) = {
    moduleConfAccessor.populateModuleConfiguration(job.module)

    if (false /*module.persistent*/) {

    } else {
      val executableFile = moduleAccessor.getModuleExecutable(job.module)

      if (executableFile.isDefined) {
        val previousModule : Option[Module] = runDataAccessor.findPreviousModule(job);

        var inStream : InputStream = null

        if (previousModule.isDefined) {
          val previousJob = job.copy
          previousJob.module = previousModule.get

          val previousOutput: File = runDataAccessor.getRunData(previousJob, "out")
          inStream = new FileInputStream(previousOutput)
        } else {
          val channelInfo = channelInfoAcessor.readChannelData(job.channel, job.channelVersion)

          inStream = new ByteArrayInputStream(channelInfo.initialInput.getBytes())
        }

        jobStatusAccessor.updateStatus(job.jobId, JobStatus.Running)

        ProcessHandler.startInstanceProcess(job, executableFile.get, inStream, job.module.timeout, moduleFinished)

        IOUtils.closeQuietly(inStream)
      }

      throw new RuntimeException("The module executable could not be found, module: " + job.module.toString)
    }

  }

  def moduleFinished(job: JobInfo, stdout: File, stderr: File, exitCode: Int): Unit = {
    if (exitCode == const.SUCCESS_CODE) {
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
    } else if (exitCode == const.RETRYABLE_ERROR_CODE || exitCode == const.TIMEOUT_CODE) {
      job.attempt += 1

      if (job.attempt > MAX_RETRIES) {
        jobStatusAccessor.updateStatus(job.jobId, JobStatus.RetriesExceeded)
      } else {
        jobStatusAccessor.updateStatus(job.jobId, JobStatus.Pending)

        /*Submit to internal job queue?*/
        run(job)
      }
    } else {
      jobStatusAccessor.updateStatus(job.jobId, JobStatus.NoRetryError)
    }

    runDataAccessor.writeRunData(job, "err", stderr)

    stdout.delete()
    stderr.delete()
  }
}
