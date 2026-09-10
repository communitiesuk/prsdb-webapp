---
applyTo: "**/application/**"
---

# Scheduled Tasks Instructions

## Overview

Scheduled tasks are ephemeral application runners that spin up, execute, and exit. They live in `application/` and implement Spring's `ApplicationRunner` interface.

## Task Runner Pattern

```kotlin
@PrsdbScheduledTask("my-task-name")
class MyTaskRunner(
    private val context: ApplicationContext,
    private val myService: MyService,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        myService.doWork()

        val code = SpringApplication.exit(context, { 0 })
        exitProcess(code)
    }
}
```

**Key points:**
- Annotate with `@PrsdbScheduledTask("task-name")` — this combines `@Component`, `@TaskName`, and conditional activation
- The task **must** call `SpringApplication.exit()` and `exitProcess()` at the end
- Inject services for business logic — keep the runner thin
- The task **must exit with a non-zero exit code if any error occurs during execution** — see [Error Handling and Exit Codes](#error-handling-and-exit-codes)

## Error Handling and Exit Codes

Production tasks run as ephemeral ECS tasks, and the infrastructure **alarms when a task exits with a non-zero exit
code**. This is the primary signal that a scheduled task has failed. It is therefore critical that **any error during
execution results in a non-zero exit code** — otherwise failures are silent and no alarm is raised.

There are two acceptable patterns, depending on whether the task should stop on the first error or process a batch to
completion:

### 1. Fail fast — let the exception propagate

If the task should abort as soon as anything goes wrong, do nothing special: an uncaught exception thrown from `run()`
causes Spring Boot to exit with a non-zero code. If you catch an exception only to log it, you **must re-throw** it:

```kotlin
override fun run(args: ApplicationArguments?) {
    try {
        service.doWork()
        exitProcess(SpringApplication.exit(context, { 0 }))
    } catch (throwable: Throwable) {
        println("Error during task execution: ${throwable.message}")
        throw throwable // re-throw so the process exits non-zero and the alarm fires
    }
}
```

### 2. Process the whole batch, then fail if any item failed

If the task processes many items and one bad item should **not** stop the others (e.g. sending a batch of emails),
catch the per-item exception, log it, continue, but **track the failures**. Return a failure count from the service /
task logic and have the runner set a non-zero exit code when it is greater than zero:

```kotlin
override fun run(args: ApplicationArguments?) {
    val failureCount = taskLogic.doWork()

    val exitCode = if (failureCount > 0) 1 else 0
    val code = SpringApplication.exit(context, { exitCode })
    exitProcess(code)
}
```

**Do not** silently swallow exceptions and exit 0 — a caught-and-logged error that still exits 0 will not raise an
alarm and the failure will go unnoticed.

> **Note on `@Transactional` batch tasks:** prefer *returning* a failure count over *throwing* after a partially
> successful batch. Throwing out of a `@Transactional` method rolls back the whole transaction, undoing the work that
> did succeed. Returning the count lets the successful work commit while the runner still exits non-zero.

## Custom Annotations

| Annotation | Purpose |
|-----------|---------|
| `@PrsdbScheduledTask` | For scheduled (recurring) tasks — requires `web-server-deactivated` + `scheduled-task` profiles and a task-specific profile |
| `@PrsdbTask` | For one-time/event-triggered tasks — requires `web-server-deactivated` profile only |
| `@PrsdbTaskService` | For services only loaded during task execution |
| `@PrsdbTaskConfiguration` | For task-specific configuration beans |
| `@TaskOnly` | Conditional bean annotation — only available during task execution |

## Profile-Based Activation

Tasks are conditionally loaded via profiles to prevent them running in web server mode:
- `web-server-deactivated` — disables the web server
- `scheduled-task` — enables scheduled task runners
- Task-specific profile (e.g. `incomplete-property-reminder-scheduled-task`)

For local testing, activate all required profiles:
```
web-server-deactivated, scheduled-task, local, my-task-scheduled-task
```

## Infrastructure

In production, tasks are triggered by **EventBridge Scheduler** which spins up ephemeral **ECS tasks**. See ADR-0029 for the architectural decision.

## Existing Tasks

| Task | Annotation | Purpose |
|------|-----------|---------|
| `ProcessScanResultTaskApplicationRunner` | `@PrsdbTask` | Process virus scan results from S3 |
| `NgdAddressUpdateTaskApplicationRunner` | `@PrsdbScheduledTask` | Load NGD address data updates |
| `IncompletePropertiesReminderTaskApplicationRunner` | `@PrsdbScheduledTask` | Send reminder emails for incomplete properties |
| `DeleteIncompletePropertiesTaskApplicationRunner` | `@PrsdbScheduledTask` | Clean up properties older than 28 days |
| `NftDataSeedingTaskApplicationRunner` | `@PrsdbTask` | Seed test data for NFT environment |
| `DefaultScheduledTaskApplicationRunner` | `@PrsdbScheduledTask` | Fallback — exits with message if no task configured |

## Adding a New Scheduled Task

1. Create the runner class in `application/` implementing `ApplicationRunner`
2. Annotate with `@PrsdbScheduledTask("your-task-name")`
3. Inject services for business logic
4. Ensure any error during execution results in a non-zero exit code (fail fast by letting exceptions propagate, or
   track failures and set the exit code — see [Error Handling and Exit Codes](#error-handling-and-exit-codes))
5. Call `SpringApplication.exit()` and `exitProcess()` at the end of `run()`
6. Add any task-only services with `@PrsdbTaskService`
