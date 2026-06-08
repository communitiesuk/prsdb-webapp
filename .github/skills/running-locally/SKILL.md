---
name: running-locally
description: Use when starting or stopping the application locally via JetBrains MCP run configurations. Covers startup, health verification, and shutdown.
allowed-tools: 'jetbrains(execute_run_configuration)'
---

# Running the Application Locally

Instructions for starting and stopping the application using JetBrains MCP run
configurations. The run configuration handles all infrastructure setup automatically.

## Starting the Application

Use the JetBrains MCP server to execute the `local` run configuration:

```
jetbrains-execute_run_configuration:
  configurationName: "local"
  projectPath: "<worktree-path>"
  waitForExit: false
  timeout: 60000
```

This single run configuration handles everything:
- Starts Docker Compose services (PostgreSQL on `POSTGRES_PORT`, Redis on `REDIS_PORT`)
- Runs Flyway migrations (via `flywayClean-local` pre-task)
- Configures Spring profiles: `local, local-no-auth`
- Starts the application on the port defined in `.env` (`SERVER_PORT`, default 8080)

**Do NOT manually run `docker-compose up` or `./gradlew bootRun`.** The run
configuration handles all of this.

## Verifying Startup

After executing the run configuration, check the console output for:
```
Started PrsdbWebappApplication in X.XXX seconds
```

The application is then accessible at:
```
http://localhost:<SERVER_PORT>/
```

Read the `SERVER_PORT` value from the worktree's `.env` file to determine the correct
port. Each worktree has a unique port assigned by the worktree creation script.

## Available Run Configurations

| Name | Purpose | Profiles |
|------|---------|----------|
| `local` | Standard local development | `local, local-no-auth` |
| `local-no-server` | Run without web server (tasks only) | `local, web-server-deactivated, scheduled-task` |
| `local-nft-seeder` | Seed NFT test data | `local, local-no-auth, nft-data-seeder` |

## Stopping the Application

When finished with local testing, stop the application. If it was started via
`execute_run_configuration`, use the JetBrains MCP to stop it:

```powershell
# PowerShell
Stop-Process -Id <PID>
```

```bash
# Bash
kill <PID>
```

Pass the command via `jetbrains-execute_terminal_command` with the worktree's
`projectPath`, or use the IDE's run tool window stop button via the MCP tools.

**Always stop the application when finished.** Running applications consume resources
and hold ports that other worktrees may need.

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Port already in use | Another worktree/process on same port | Check `.env` for assigned port; stop conflicting process |
| Docker not running | Docker Desktop not started | Start Docker Desktop before running |
| Database migration failure | PostgreSQL not accessible | Ensure Docker started and port matches `.env` |
| Redis connection refused | Redis container not running | Docker Compose services may have failed to start |

## Notes

- The `local-no-auth` profile provides mock authentication with all roles — no real
  One Login integration needed for local development
- Redis is used for session storage; if Redis is not running, the application fails to start
- The application serves on HTTP (not HTTPS) locally
- The `flywayClean-local` pre-task drops and recreates the database schema on every
  start — local data does not persist between runs
