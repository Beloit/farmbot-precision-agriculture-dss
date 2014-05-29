package mains

import types.FarmChannel
import dynamo.FarmChannelAccessor
import dynamo.ResourceTableAccessor
import dynamo.JobStatusAccessor
import s3.ChannelInfoAccessor
import types.ChannelInfo
import types.Module
import types.JobInfo
import constants.JobStatusTableConstants
import constants.JobStatusTableConstants.JobStatus
import constants.JobStatusTableConstants.JobStatus.JobStatus
import aws.AWSInitialization

object MasterMain extends App {
  // scan farm channel table (based on schedules and current time)
  // should return farm channels that are currently ready
  val farmChannelAccessor : FarmChannelAccessor = new FarmChannelAccessor()
  val resourceTableAccessor : ResourceTableAccessor = new ResourceTableAccessor()
  val channelInfoAccessor : ChannelInfoAccessor = new ChannelInfoAccessor()
  val jobStatusAccessor : JobStatusAccessor = new JobStatusAccessor()
  
  //val FIVE_MINUTES = 300000
  val FIVE_MINUTES = 60000
  
  var startTime : Long = 0
  var timeDiff : Long = 0

   AWSInitialization.setup
   forever (
     createReadyJobs
   )

  def forever[A](body: => A): Nothing = {
    body
    
    forever(body)
  }
  
  def createReadyJobs() {  
    startTime = System.currentTimeMillis()

    var farmChannels : Seq[FarmChannel] = farmChannelAccessor.getReadyJobs()

    // for each ready farm channel:
    // farmChannel.id => key into resource table (needs to be created and accessor written)
    // get ResourceTable for farmChannel.id will get all the resourceIds for that farm channel
    for (farmChannel <- farmChannels) {
      var resources : Seq[String] = resourceTableAccessor.getResources(farmChannel.id)

      // ChannelInfo => get ChannelInfo from accessor for the channel and version from the farm channel object
      // get list of modules for the channel from the ChannelInfo
      var channelInfo : ChannelInfo = channelInfoAccessor.readChannelData(farmChannel.channel, farmChannel.version)
      var modules : List[Module] = channelInfo.modules 
    
      // for each resourceId and module, create linked list of JobInfo's (set first job to ready, rest to pending)
      // put each job into job status table (in reverse order to avoid race condition)
      
      addJobsForChannel(resources, modules, farmChannel, channelInfo)
    } 
    
    timeDiff = System.currentTimeMillis() - startTime
       
    if (timeDiff < FIVE_MINUTES) {
      Thread.sleep(FIVE_MINUTES - timeDiff)
    }
  }
  
  def addJobsForChannel(resources : Seq[String], modules : List[Module], farmChannel : FarmChannel, channelInfo : ChannelInfo) {
    for (resourceID : String <- resources) {
      var previous : JobInfo = null
      var current : JobInfo = null
      var head : JobInfo = null
      var i : Int = 0
       
      // create linked list of jobs for current resource
      for (curModule : Module <- modules) {
        previous = current
        current = new JobInfo {
          farmChannelId = farmChannel.id
          farmId = farmChannel.farmID
          resourceId = resourceID
          channel = channelInfo.name
          channelVersion = channelInfo.version
          module = curModule
          nextId = null
        }
         
        if (previous != null) {
          current.previousId = Option(previous.jobId)
        } else {
          current.previousId = null
        }
         
        previous.nextId = Option(current.jobId)
         
        // add previous entry to job table (initialized as pending)
        jobStatusAccessor.addEntry(previous)
         
        if (i == 0) {
          head = current;
        }
         
        i += 1
      }
       
      // add the last entry to job table
      jobStatusAccessor.addEntry(current)
       
      // set first job (head) status as ready
      jobStatusAccessor.updateStatus(head.farmChannelId, head.jobId, JobStatus.Ready, JobStatus.Pending)
    }
  }
}