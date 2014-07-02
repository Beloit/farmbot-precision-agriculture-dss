farmbot-precision-agriculture-dss
=================================
For a high level overview of what the Decision Support System (DSS) does, as well as context of how it fits into the other FarmBot software components, see the wiki page at: http://farmbot.cc/wiki/Decision_Support_System

Technical documentation coming soon here on GitHub

# Technical Documentation
## Summary
The Farmbot Decision Support System (DSS) Engine coordinates all of the channels
and modules, scheduling and running the modules for each channel, producing an 
optimized schedule of operations for the Farmbot to grow plants. The DSS Engine
is written in Scala, an object-functional programming language. Scala is
compiled to Java byte code and is run on the JVM. Another key component of the
DSS Engine is the use of Amazon Web Services (AWS) DynamoDB and S3 services, for
storing information needed by the Farmbot DSS such as channel configuration and
modules, as well as communication status information within the DSS Engine, such
as job statuses.

The DSS Engine has two main programs, which run simultaneously. The MasterMain 
program checks farm channel schedules to find channels that are ready to be run,
and creates the jobs for the channel, in a linked list structure so that each 
job for the channel is run in the correct order. The SlaveMain program checks
for and runs ready jobs, updating job statuses along the way. Each job in a 
channel passes its output to the next job as input, with the output of the final
job for a channel being POSTed to the Farmbot website, with the optimized
schedule of instructions for the Farmbot.

## Master Main
The Master Main program runs continually, executing the following process every
5 minutes. First, the Farm Channel DynamoDB table is scanned (based on the
schedule for the channel and the current time), which returns the farm channels
that are ready to be run. For each ready Farm Channel, the MasterMain gets the
resourceIds for the farm channel, by using the farm channel id as a key into the
Resource DynamoDB table. The list of modules for a channel is retrieved from the
channel configuration, which is retrieved from the farmbot-dss-channels S3
bucket, with a key of the form "farmChannel_version". For each resource, a
linked list of jobs for each module is created, setting the first job to 'ready'
 and the rest to 'pending'. Each job is added to the JobStatus table.

## Slave Main
The Slave Main program also funs continually, repeating the following process.
SlaveMain scans the JobStatus table for ready jobs. When a ready job is found,
the executable module for that job is run. The stdout and stderr for the
module run are each written to the farmbot-dss-rundata S3 bucket. The status of
the job is updated based on the result of the run, if successful the next job in
the list is changed from pending to ready. For the final job for a farm
channel resource, the output of the optimized Farmbot instructions is sent to
the Farmbot website through and HTTP POST.

## AWS

### DynamoDB
The DSS Engine uses several DynamoDB tables, which are primarily used for
communicating status and other information internal to the DSS. 

#### farmbot-dss-ChannelFarm
The key into this table is ChannelAndVersion, which is of the form
"channelName_version". The table also has entries for FarmId (Int), 
LastRunTime (Int), OpaqueId (String), ScheduleHour(Int, Optional), and
ScheduleMinute (Int). The LastRunTime and ScheduleHour/ScheduleMinute fields
are used to determine whether a channel is ready to be run. If a channel does
not have a ScheduleHour entry, this means it is run hourly, after the
ScheduleMinute. Otherwise, the channel is run daily, after the defined
ScheduleHour:ScheduleMinute each day. For example, a channel with ScheduleHour =
10 and ScheduleMinute = 15 will be ran once a day after 10:15. A channel with
no ScheduleHour and ScheduleMinute 30 will be ran hourly, 30 minutes after each
hour. To determine whether a channel is ready to be run by the DSS, the engine
checks this table to determine which channels are ready to be run.

#### farmbot-dss-FarmResources
The primary key into this table is FarmChannelOpaqueId, a String. The other
entry is ResourceId, a String. This is a one-to-many table, so each OpaqueId
may have many entries in the table, with different resourceIds. This allows
a FarmChannel to have multiple resources in the DSS Engine. When a channel is
ready to be run, the DSS gets a list of the resources for the channel from this
table. Then, for each resource in the channel, a list of jobs is created, one
for each module in order.

#### farmbot-dss-JobStatus
This table is used in the DSS Engine to store status information on the jobs. 
The most important fields are the "Status", "NextId", and "PreviousId" fields.
The status field may be "Ready", "Pending", "Running", "Success", or error 
statuses (such as "ErrorInvalidOutput" or "TimedOut"). "Ready" means a job is 
ready to be run and "Pending" means a job is waiting for another job to be run 
before it can be run. The "NextId" field defines the next job id that is ready
to be run after this job. The "PreviousId" field defines the previous job id
that was run before this job. This creates the linked list structure of jobs. 
When the list of jobs is created for a farm channel's resource, the first job
is set to "Ready" with others set to "Pending". When a job is finished, it sets
its NextId job to "Ready" state. This is how the order of jobs is enforced.

#### farmbot-dss-ModuleConfiguration
The primary key of this table is ModuleName, of the format "moduleName_version".
There is also a boolean entry Persistent, and an integer entry Timeout. These
define whether a module is persistent, and how many milliseconds before a module
times out, respectively


#### farmbot-dss-ModuleConfiguration
### S3
The DSS Engine uses several S3 buckets, each of which is described below

#### farmbot-dss-channels
The farmbot-dss-channels bucket is used to store Farmbot channel configurations.
The keys into this bucket are of the format "channelName_version". Each file
in the S3 bucket is a file holding the channel configuration JSON. The channel
configuration JSON should fit the following schema:    
   <pre><code> {    
      "name" : String,    
      "version" : Int,    
      "modules" : List[Modules],    
      "metadata" : Map,    
      "initialInput" : JSON,    
      "schema" : JSON Schema    
   } </code></pre>
For the list of Modules, each module should fit the schema:    
   <pre><code> {    
      "version" : Int,    
      "name" : String,    
      "persistent" : boolean,    
      "timeout" : Int    
   } </code></pre>
The schema field defines the JSON schema that the output of modules must adhere
to. The initial input field's JSON must also fit this schema.

#### farmbot-dss-modules
This S3 bucket contains the modules which the DSS Engine runs for channels. The
key for this table is "moduleName_version". A module can be any arbitrary 
executable file.

#### farmbot-dss-rundata
This S3 bucket stores the stderr and stdout for each job that the DSS Engine
runs. The stdout run data of each job is passed as the input to the next job. 
Another important use of the run data is for debugging when errors arise.



## Integration
In order to integrate the DSS Engine with the rest of the Farmbot systems, the
main work needed is to update the DynamoDB tables and S3 buckets when modules
are created or farm channels are configured on the Farmbot website. Other than
that, servers need to be setup to run the MasterMain and SlaveMain programs. The
DSS Engine was created to continuously run, checking for and running ready jobs
as described above. 

When a Farmbot module is created, the executable file needs
to be put in the farmbot-dss-modules bucket under the key "moduleName_version". An entry for this module also needs to be added to the 
farmbot-dss-ModuleConfiguration table, defining a timeout for the module and
whether it is persistent.

When a Farmbot farm channel is configured on the Farmbot website, the
appropriate S3 buckets and DynamoDB tables need to be updated for this channel:
farmbot-dss-ChannelFarm and farmbot-dss-FarmResources tables,
farmbot-dss-channels bucket.

AWS has APIs for Java, .Net, Node.js, PHP, Python, and Ruby. The API 
documenation can provide instruction on how to update the tables and buckets.
For an example of the use of the AWS API, the files in
src/main/scala/aws/{dynamo,s3} use the Scala AWS API (a wrapper around the
Java API) to add as well as retrieve entries from the Farmbot DynamoDB tables
and S3 buckets.

Once the servers are set up to run the DSS Engine main programs, and the Farmbot
website is configured to update the appropriate tables and buckets when a 
module or farm channel is added, the DSS Engine is ready to run. The final
output for each resource in each channel (from the final module) is sent to
the Farmbot website in an HTTP POST.

