---
applyTo: "**/application/**,**/annotations/taskAnnotations/**"
---

# Scheduled Tasks Instructions

## Overview

Scheduled tasks are ephemeral application runners that spin up, execute, and exit. They live in `application/` and implement Spring's `ApplicationRunner` interface.

## Task Runner Pattern

```kotlin
@PrsdbScheduledTask("my-task-scheduled-task")
class MyTaskApplicationRunner(
    private val context: ApplicationContext,
    private val taskLogic: MyTaskLogic,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        taskLogic.doWork() // Returns to the runner after the transaction completes

        val code = SpringApplication.exit(context, { 0 })
        exitProcess(code)
    }
}

@PrsdbTaskService
class MyTaskLogic(
    private val myService: MyService,
) {
    @Transactional
    fun doWork() {
        myService.doWork()
    }
}
```

**Key points:**
- Annotate with `@PrsdbScheduledTask("task-name")` — this combines `@Component`, `@TaskName`, and conditional activation
- After successful work (or a completed batch), call `SpringApplication.exit()` and `exitProcess()` with the appropriate code
- Keep the runner thin. A separate, injected `@PrsdbTaskService` task-logic collaborator can own `@Transactional` work,
  returning/committing **before** the runner exits. Do not terminate the process inside a transaction or annotate `run()`
  as transactional and exit before its proxy can commit.
- The task **must exit with a non-zero exit code if any error occurs during execution** — see [Error Handling and Exit Codes](#error-handling-and-exit-codes)

This transaction boundary is used by
[DeleteIncompletePropertiesTaskApplicationRunner](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/application/DeleteIncompletePropertiesTaskApplicationRunner.kt),
[DeleteExpiredJointLandlordInvitationsTaskApplicationRunner](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/application/DeleteExpiredJointLandlordInvitationsTaskApplicationRunner.kt)
and [JointLandlordInvitationExpiryEmailTaskApplicationRunner](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/application/JointLandlordInvitationExpiryEmailTaskApplicationRunner.kt).

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
alarm and the failure will go unnoticed. Never put a blanket successful exit in `finally`; it can mask the original failure.

> **Note on `@Transactional` batch tasks:** prefer *returning* a failure count over *throwing* after a partially
> successful batch. Throwing to signal failures can roll back successful work. Return the count from the transactional
> collaborator so valid successes can commit before the runner exits non-zero. Returning a count cannot rescue a transaction
> already marked rollback-only; choose per-item transaction boundaries where necessary.

## Custom Annotations

| Annotation / condition | Purpose |
|------------------------|---------|
| `@PrsdbScheduledTask("name")` | Requires `web-server-deactivated`, `scheduled-task` and the exact `name` profile |
| `@PrsdbTask("name")` | One-time/event-triggered runner; requires `web-server-deactivated` and the exact `name` profile |
| `@PrsdbTaskService` | Task-mode service; guarded only by `TaskOnly`, not a particular task name |
| `@PrsdbTaskConfiguration` | Task-mode configuration; guarded only by `TaskOnly` |
| `TaskOnly` | Spring `Condition` implementation checking `web-server-deactivated`; **not** an `@TaskOnly` annotation |

## Profile-Based Activation

[TaskHasName / ScheduledTaskHasName](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/annotations/taskAnnotations/TaskHasName.kt)
control runner activation:
- `web-server-deactivated` is required for both runner types and disables the web server via `application.yml`.
- `scheduled-task` is additionally required for scheduled runners.
- A non-blank annotation name must exactly match an active task-specific profile.
- A blank name bypasses **only the name check**, not the task-mode or scheduled-mode checks.

For the example above, the matching local profile set is:
```text
web-server-deactivated, scheduled-task, local, my-task-scheduled-task
```

Activate one intended task-name profile per process. Multiple task profiles do not form a batch: each runner exits the
process, so later runners will not execute.

[DefaultScheduledTaskApplicationRunner](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/application/DefaultScheduledTaskApplicationRunner.kt)
has a blank name and `Ordered.LOWEST_PRECEDENCE`. It is always eligible in scheduled task mode, **not**
`@ConditionalOnMissingBean`. If reached (no selected runner, or a runner returned without exiting), it logs the
configuration failure and exits **1**.

## Infrastructure

In production, tasks are triggered by **EventBridge Scheduler** which spins up ephemeral **ECS tasks**. See ADR-0029 for the architectural decision.

## Existing Tasks

| Task | Annotation | Purpose |
|------|-----------|---------|
| `ProcessScanResultTaskApplicationRunner` | `@PrsdbTask` | Process virus scan results from S3 |
| `NgdAddressUpdateTaskApplicationRunner` | `@PrsdbScheduledTask` | Load NGD address data updates |
| `IncompletePropertiesReminderTaskApplicationRunner` | `@PrsdbScheduledTask` | Send reminder emails for incomplete properties |
| `DeleteIncompletePropertiesTaskApplicationRunner` | `@PrsdbScheduledTask` | Clean up properties older than 28 days |
| `DeleteExpiredJointLandlordInvitationsTaskApplicationRunner` | `@PrsdbScheduledTask` | Delete expired joint invitations (`jl-invitation-deletion-scheduled-task`) |
| `JointLandlordInvitationExpiryEmailTaskApplicationRunner` | `@PrsdbScheduledTask` | Send joint-invitation expiry emails (`jl-invitation-expiry-email-scheduled-task`) |
| `NftDataSeedingTaskApplicationRunner` | `@PrsdbTask` | Seed test data for NFT environment |
| `DefaultScheduledTaskApplicationRunner` | `@PrsdbScheduledTask` | Always-eligible scheduled-mode guard at lowest precedence; exits 1 if reached |

## Adding a New Scheduled Task

1. Create the runner class in `application/` implementing `ApplicationRunner`
2. Annotate with `@PrsdbScheduledTask("my-task-scheduled-task")` and use that exact task-name profile
3. Inject services/task logic for business work; put transactions on a separate collaborator that returns before exit
4. Ensure any error during execution results in a non-zero exit code (fail fast by letting exceptions propagate, or
   track failures and set the exit code — see [Error Handling and Exit Codes](#error-handling-and-exit-codes))
5. After work/commit completes, call `SpringApplication.exit()` and `exitProcess()` with the correct exit code
6. Add any task-only services with `@PrsdbTaskService`

See [config instructions](config.instructions.md#custom-annotations-for-config-classes) for shared versus mode-specific
beans and [service instructions](services.instructions.md) for business-logic conventions.
