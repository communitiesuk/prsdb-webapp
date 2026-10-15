---
name: fix-failing-pr-tests
description: Use when a pull request has failing tests to aid the developer with fixing. Usage /fix-failing-pr-tests <link-to-pull-request>
allowed-tools: powershell read_powershell grep glob view edit ask_user
---

For doing markups of PRs after they've been reviewed:
1. Find the PR's failing checks. Use `gh pr view <number> --repo <repo> --json headRefOid,statusCheckRollup` to get the HEAD SHA and the status of ALL checks.
2. Never check the status of the "Build and Test / Build and test" run, since this only consolidates the shard fails into a single fail.
3. Use `gh run list --repo <repo> --commit <sha>` to find the correct run ID, then `gh run view <id> --repo <repo> --log` to get the logs.
4. You can tell if it's a test fail or a lint fail by inspecting the logs
    1. Test fails will have "> Task :test" around line 450
    2. Lint fails will have "KtLint found code style violations. Please see the following reports:" around line 350. In this case, run the `ktlintFormat` gradle task and finish.
5. Start by summarising each test fail, grouping them by test fails that seem to be from the same root cause.
6. Wait for the user to provide any guidance.
7. Then, go through each test fail group one by one, and suggest a fix for the test passing. Provide some example code if possible.
8. If the user says yes, make the change, if the user says no, move on to the next test fail.
9. Do this until all test fails are resolved.
