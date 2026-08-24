---
name: running-builds
description: Use when building or compiling prsdb-webapp, including incremental builds, full rebuilds, and diagnosing build failures.
allowed-tools: 'jetbrains'
---

# Running builds

1. Use `build_project` with the repository `projectPath` and a suitable timeout.
2. Use the default incremental build.
3. Read the MCP result. `build_project` returns compilation errors and warnings
   directly.
4. A returned build error means the build failed. An MCP error or timeout means
   the build result is blocked or unknown, not failed.
