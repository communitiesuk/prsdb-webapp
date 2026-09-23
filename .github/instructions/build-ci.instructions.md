---
applyTo: "*.gradle.kts,gradle.properties,gradle/**,gradlew,gradlew.bat,package.json,package-lock.json,rollup.config.mjs,.github/workflows/**"
---

# Build and CI Instructions

## Sources of Truth

- `build.gradle.kts` defines plugins, dependencies, the Java 21 toolchain and Gradle tasks.
- `package.json`, `package-lock.json` and `rollup.config.mjs` define frontend dependencies, scripts and outputs.
- `.github/workflows/build-and-test.yml` uses Java 21 and Node 22. Keep local/toolchain guidance consistent with
  these declarations; do not maintain separate copies of every dependency version in instructions.
- Use the existing wrapper and package managers. Update lockfiles with their package manager when dependencies change.

## Frontend and JVM Build Dependencies

The existing chain is:

```text
npm run build -> dist/
buildFrontendAssets -> copyBuiltAssets -> build/resources/main/static/assets/
KotlinCompile and Test tasks depend on copyBuiltAssets
```

`copyBuiltAssets` depends on `buildFrontendAssets`, which invokes `npm run build` using a Windows-aware command.
It copies the Rollup output, not `src/main/resources/assets/`. Generated `dist/` and `build/` files are not source
to edit. Even targeted Kotlin compilation or JVM testing can require Node/npm and installed frontend dependencies.
See [frontend.instructions.md](frontend.instructions.md) for source locations and bundling conventions.

## Test Task Boundaries

| Task / command | Scope |
|----------------|-------|
| `gradlew build` | Full Gradle build/check lifecycle, including frontend assets |
| `gradlew test` | JVM tests, including browser integration and context tests |
| `gradlew test --tests "<selector>"` | Selected JVM classes or methods; use actual descriptive method names |
| `gradlew testWithoutIntegration` | Excludes the `webapp/integration` package, not every container-backed test |
| `npm test` | Separate Node frontend tests |
| `gradlew playwright` | Playwright CLI `JavaExec` task, not an integration-test suite |

On PowerShell invoke the wrapper as `.\gradlew`; on Bash use `./gradlew`. There is no separately declared
`integrationTest` task: select integration classes through `test`.

Do not describe `testWithoutIntegration` as Docker-free. Application-context tests and
`JourneyFrameworkComponentTests` live outside the excluded package and can require the shared PostgreSQL/Redis
containers. Browser fixtures and optional live Notify contracts have additional prerequisites; see
[integration-tests.instructions.md](integration-tests.instructions.md) and [emails.instructions.md](emails.instructions.md).

Follow the existing approval rules before running tests; these task descriptions do not grant permission to run them.

## Sharding and Required Checks

CI runs four test shards using paired `shardIndex` / `shardCount` Gradle properties. Indices are one-based.
The top-level class determines the shard, and nested classes stay with their enclosing class. Shards use separate
test-result/report directories; preserve this when changing shard behaviour or local parallel execution.

The workflow runs npm tests separately from Gradle tests and has an independent lint job. Preserve the aggregate
required check named **Build and test**:

- It depends on both test shards and lint.
- `if: always()` ensures it runs even when a dependency fails.
- It explicitly fails unless both dependency results are successful; a skipped required job must not mask a failure.

Keep merge-queue (`merge_group`) coverage as well as pull-request coverage when changing triggers. Deployment/reset
workflows are separate operational surfaces; do not alter their environment or approval boundaries as incidental
build cleanup.

## Configuration and Generated Files

CI installs dependencies with `npm ci` and creates test One Login keys during the job. Do not commit credentials or
generated private keys. Preserve LF line endings: CI checks tracked files for CRLF/mixed endings.

The Gradle `.env` loader configures the local Flyway plugin's database connection; it does not configure deployed
Spring environments. Keep build-time local settings distinct from `application*.yml` runtime configuration.
