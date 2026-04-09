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
4. Call `SpringApplication.exit()` and `exitProcess()` at the end of `run()`
5. Add any task-only services with `@PrsdbTaskService`
