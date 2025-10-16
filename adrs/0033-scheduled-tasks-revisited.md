TODO: Update ADR number

# ADR-0033: Scheduled tasks revisited

## Status

Proposed

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

We need multiple different jobs that will run periodically, e.g. cleaning up old partial property registrations, sending
out various different reminders to users etc. We previously decided to run these as ephemeral copies of the WebApp container
triggered by Eventbridge Scheduler [{ADR-0029}](0029-scheduled-tasks.md). However, this approach has some limitations:

* There is a limit of 10 instances of each task definition at once, so if we have a large number of scheduled tasks that all run at the same
  time, some of them may fail to start.
* The startup time of the container is long and requires a lot of resources, making it wasteful to spin up a new container for each task
  run.
* If a task fails partway through, there is no easy way to retry it.

## Considered Options

* Long running ECS task with webserver and private endpoints
* Separate ECS service with SQS queue and custom scaling rule
* Switching to Lambda functions

## Decision Outcome

Separate ECS service with SQS queue and custom scaling rule, because it allows easy reuse of existing code while only using the resources it
requires.

## Pros and Cons of the Options

### Long running ECS task with webserver and private endpoints

We could run a mirror of our existing ECS service, with a private loadbalancer that is only accessible from within our VPC and endpoints
that could be targeted by Eventbridge Scheduler.

* Good, because it addresses the limit of 10 instances of each task definition.
* Good, because it avoids the long startup time of the container.
* Good, because it would allow very easy reuse and sharing of code and patterns between the WebApp and asynchronous tasks.
* Good, because we could use normal scaling rules for the ECS service based on the load of the containers.
* Good, because it would be easy to trigger asynchronous tasks using either Eventbridge Scheduler or by sending a request from the WebApp.
* Bad, because we would have to handle retry logic ourselves if a task failed partway through, and there would be some cases where
  tasks could be lost if the container was stopped or restarted while processing a task.
* Bad, because it would require a full copy of the WebApp to be running all the time even when there are no asynchronous tasks, which is
  wasteful in terms of resources.

### Separate ECS service with SQS queue and custom scaling rule

We could extend our current approach of having an ephemeral non-webserver version of the WebApp container, to instead have a long-running
ECS service that reads tasks from an SQS queue. Eventbridge Scheduler would then send messages to the SQS queue to trigger tasks. We could
set up a custom scaling rule for the ECS service based on the average number of messages in the SQS queue per task, so that it would scale
up when there are tasks to process and scale down to zero when there are no tasks.

* Good, because would allow relatively easy reuse of our existing code.
* Good, because during high volumes of tasks it would avoid the long start-up time of the webapp container.
* Good, because it would scale down to zero when there are no tasks, avoiding wasteful resource usage.
* Good, because it addresses the limit of 10 instances of each task definition.
* Good, because it would allow us to implement retry logic using the built-in features of SQS, and would avoid losing tasks if a container
  was stopped or restarted while processing a task.
* Good, because it would be easy to trigger asynchronous tasks using either Eventbridge Scheduler or by adding a message to the queue from
  the WebApp.
* Bad, because it would require some changes to our existing code to poll the queue for messages instead of receiving them via an
  environment
  variable.
* Bad, because it would require creating a custom scaling rule for the ECS service, which is more complex than using the built-in scaling
  rules.

### Switching to Lambda functions

We could split out each scheduled task into a separate Lambda function, which would be triggered by Eventbridge Schedulers. Each Lambda
function

* Good, because we wouldn't need to worry about scaling rules.
* Good, because we would only be using resources when a task is actually running.
* Good, because it would avoid the long start-up time of the full webapp container.
* Good, because it would address the limit of 10 instances of each task definition.
* Neutral, because while we wouldn't get retry logic for free, it would be relatively trivial to set up an SQS queue per lambda to allow for
  this.
* Neutral, because while we wouldn't get the ability to trigger asynchronous tasks for free, it would be relatively trivial to set up an SQS
  queue per lambda to allow for this.
* Bad, because we would need to significantly refactor our existing code to split out common functionality into a separate package that
  could be used by the Lambda functions.
* Bad, because we would need to ensure no tasks take longer than 15 minutes to run, which may not be possible for some tasks.

## More Information

Custom ECS scaling rules: https://aws.amazon.com/blogs/containers/amazon-elastic-container-service-ecs-auto-scaling-using-custom-metrics/
