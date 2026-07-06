---
name: fix-failing-pr-tests
description: Use when a pull request has failing tests to aid the developer with fixing. Usage /fix-failing-pr-tests <link-to-pull-request>
allowed-tools: powershell read_powershell grep glob view edit ask_user
---

For doing markups of PRs after they've been reviewed:
1. Use GitHub MCP to find the PR and the status of the last run.
2. You can tell if it's a test fail or a lint fail by inspecting the logs
    1. Test fails will have "> Task :test" around line 450
    2. Lint fails will have "KtLint found code style violations. Please see the following reports:" around line 350. In this case, run the `ktlintFormat` gradle task and finish.
3. Start by summarising each test fail, grouping them by test fails that seem to be from the same root cause.
4. Wait for the user to provide any guidance.
5. Then, go through each test fail group one by one, and suggest a fix for the test passing. Provide some example code if possible.
6. If the user says yes, make the change, if the user says no, move on to the next test fail.
7. Do this until all test fails are resolved.
