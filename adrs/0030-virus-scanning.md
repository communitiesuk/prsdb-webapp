# ADR-0030: Virus Scanning

## Status

Draft

Date of decision:

## Context and Problem Statement

Our application allows users to upload files to a quarantine S3 bucket. We need to implement virus scanning
using ClamAV on ECS to protect our system and users. The scanning process must:

1. Run on ECS
2. Scan files in a timely manner after upload
3. Communicate scan results and file status back to the webapp
4. Move clean files to the appropriate S3 bucket
5. Send email notifications for infected files

Key decisions needed:

- How to trigger and orchestrate the scanning process
- How to store the scan results in the database for use by the webapp
- How to send notifications back to the user as needed

Each of these are considered separately, although there is some interaction between the different options

## Considered Options

### Considered Options for triggering virus scan

* S3 Event -> Eventbridge -> Ephemeral ECS task
* S3 Event -> SQS queue -> Long running ECS task
* Direct invocation from webapp via AWS SDK -> Ephemeral ECS task
* API call from webapp -> Long running ECS task

### Considered Options for storing status/ results in the database

* Virus scanner task -> RDS
* Virus scanner task -> S3 -> (polling, or checking lazily) Webapp -> RDS
* S3 Event -> SQS queue -> scheduled task solution (see ADR-0029) -> RDS
* Virus scanner -> (API call) Webapp -> RDS

### Considered Options for triggering email notification back to the user

* Virus scanner task -> Notify
* Webapp -> Notify (once file status is determined)
* S3 Event -> SQS queue -> scheduled task solution (see ADR-0029)
* S3 Event -> Eventbridge -> scheduled task solution (see ADR-0029)

## Decision Outcome

TBC - two lead combinations of options are:

1. Virus scanner task has access to RDS, Notify, and runs a webserver allowing the webapp to trigger scans directly
2. All communication is handled by S3 Events and SQS queues, with our scheduled task solution processing the queues,
   writing results to RDS and sending notifications

Option 1 is the simpler architecture and likely faster to implement, but option 2 is in theory more robust and secure (
although there may be little difference in practice and could be overkill for our use case).

## Pros and Cons of the Options

### Options for triggering virus scan

#### S3 Event -> Eventbridge -> Ephemeral ECS task

We could use an s3 event (the file being put into the quarantine bucket) to trigger an ephemeral ECS task via
Eventbridge. The task would then grab the object that triggered the event and scan it.

* Good, because it is a relatively simple architecture
* Good, because we are not using ECS resources when we don't need them
* Good, because it is similar to the pattern used by some other services, e.g. elections
* Bad, because a high volume of uploads would be a large number of ECS tasks being spun up at once
* Bad, because if we are unable to spin up a new ECS task for some reason we might miss events
* Bad, because we couldn't retry a scan if something went wrong, e.g. the virus scanner crashed

#### S3 Event -> SQS queue -> Long running ECS task

We could use an s3 event (the file being put into the quarantine bucket) to trigger a message being added to an SQS
queue, with a long running ECS task that consumes the messages on that queue (by grabbing the file mentioned in the
queue message and scanning it).

* Good, because we would only spin up additional tasks if autoscaling rules were triggered
* Good, because we can use a combination of retries and a dead-letter queue to ensure we don't miss any messages
* Good, because the SQS queue can be setup to retry messages where the ECS task doesn't report success
* Bad, because it's a more complex architecture, including requiring custom autoscaling rules
* Bad, because we will still have a running ECS task even when no one is uploading files

#### Direct invocation from webapp via AWS SDK -> Ephemeral ECS task

Rather than relying on S3 events, we could take advantage of the fact that files are uploaded via our webapp, and kick
off virus scanning directly. One option for doing that would be to spin up an ephemeral ECS task using the AWS SDK,
sending it a command to run a scan on the file that was just uploaded.

* Good, because we have more control over when and how the virus scan is triggered
* Good, because it wouldn't require any additional infrastructure
* Good, because we are not using ECS resources when we don't need them
* Bad, because kicking of the new task would still need to be done asynchronously, meaning that if we are unable to spin
  up a new ECS task for some reason we might miss scanning some files
* Bad, because we couldn't retry a scan if something went wrong, e.g. the virus scanner crashed

#### API call from webapp -> Long running ECS task

We could run a lightweight webserver in the same container as the virus scanner, only reachable from within our VPN, and
have the Webapp call it to trigger a virus scan.

* Good, because we would have full control over how and when the virus scan is triggered, and the communication between
  the webapp and the virus scanner
* Good, because it is similar to the pattern used by some other Gov (but non-MHCLG) services, e.g. services from DWP &
  HMRC
* Bad, because we will still have a running ECS task even when no one is uploading files
* Bad, because we would need a load balancer to handle the case where autoscaling requires more than one virus scanning
  container
* Neutral, because while it would require us to create a webserver on the virus scanning container it would only need to
  expose a very simple API
* Neutral, because it would require additional infrastructure configuration to ensure that the webserver in the virus
  scanner can only be reached from inside our VPC
* Bad, because we could not retry a scan if something went wrong, e.g. the virus scanner crashed

### Options for communicating status/ results

#### Virus scanner task -> RDS

We could track the status of the virus scan by having the virus scanner write the status of the scan to a table in RDS.

* Good, because it is simple to implement
* Good, because we can use transactions to avoid race conditions between the webapp and the virus scanner
* Bad, because it would require the virus scanner to have access to RDS, and therefore the database credentials,
  providing an additional attack surface for the application

#### Virus scanner task -> S3 -> (polling, or checking lazily) Webapp -> RDS

We could track the status of the virus scan by having the virus scanner write the status of the scan to a file in S3.

* Good, because it does not require additional permissions for the virus scanner
* Bad, because care would need to be taken not to introduce race conditions between the webapp and the virus scanner,
  given the lack of transactions
* Bad, because it will require the webapp to either poll s3 or check lazily for changes to the file, meaning that there
  could be a delay between the virus scanner writing the status and the webapp being aware of it

#### S3 Event -> SQS queue -> scheduled task solution (see ADR-0029) -> RDS

When the virus scanner places a new file in the 'clean files' bucket, or a placeholder file in an 'infected files'
bucket, we could use the event that is triggered to add a message to an SQS queue, which could be picked up by our
scheduled task solution

* Good, because it does not require additional permissions for the virus scanner
* Good, because we can use transactions when writing to RDS to avoid race conditions between the webapp and the virus
  scanner
* Good, because we can reuse our containers and logic that we intend to use for scheduled tasks to process the queue
* Good, because we can use retry logic and a dead-letter queue to ensure we don't fail to record the results of a scan
  due to the webapp being down
* Bad, because it is a more complex architecture

#### Virus scanner -> (API call) Webapp -> RDS

The virus scanner could call the webapp API to report the results of the scan so that the webapp can update RDS

* Good, because it does not require additional permissions for the virus scanner
* Good, because we can maintain the logic relating to storing/ updating/ retrieving data about the file in the webapp
* Good, because it avoids race conditions between the webapp and the virus scanner
* Bad, because we would need retry logic in the virus scanner to ensure that results aren't lost if the webapp is down
  when the results are posted
* Neutral, because it would require additional infrastructure configuration to ensure that the endpoints for receiving
  updates from the virus scanner can only be reached from inside our VPC
* Bad, because it would further increase the load on the webapp if lots of files are uploaded in a short space of time

### Options for triggering email notifications

#### Virus scanner task -> Notify

The virus scanner could include code/ credentials to call Notify directly, and send an email to the user when a virus is
detected

* Good, because it would be simple to implement
* Good, because it would not require any additional infrastructure
* Good, because it would keep any gap between the virus being detected and alerting the user to a minimum
* Bad, because it would require the virus scanner to have access to details such as the property that the file relates
  to and the email address of the landlord, either via database access or by adding metadata to the file

#### Webapp -> Notify (once file status is determined)

Once the webapp becomes aware (via whatever mechanism is determined above) that a virus has been found, it can send the
email via Notify

* Good, because it would be simple to implement
* Good, because it would not require any additional infrastructure
* Good, because it wouldn't require the virus scanner to have access to details such as the property that the file
  relates to and the email address of the landlord
* Neutral/ Bad, depending on the timing of the webapp becoming 'aware' of the scan result, because it could lead to long
  delays in notifying the user that a virus has been detected

#### S3 Event -> SQS queue -> scheduled task solution (see ADR-0029)

When the scanned file/ virus placeholder is placed in the relevant S3 bucket, we could use the S3 event generated to
place a message on a SQS queue, which would be picked up by our scheduled task solution, which would then call Notify.

* Good, because it wouldn't require the virus scanner to have access to details such as the property that the file
  relates to and the email address of the landlord
* Good, because it doesn't require the webapp to poll for the status of the files
* Good, because we can use retry logic and a dead-letter queue to ensure we don't fail to notify the user due to the
  webapp being down
* Bad (unless the same approach is used to communicate the results of the virus scan), because it would require a more
  complex architecture with additional infrastructure
