# General Instructions

When acting as an agent, you MUST plan extensively before each function call, and reflect extensively on the outcomes of the previous function calls. DO NOT do this entire process by making function calls only, as this can impair your ability to solve the problem and think insightfully.

When acting as an agent, you MUST NEVER proceed to a new step without explicit approval for that step, otherwise. After completing each step, discuss which steps you have approval for. Proceeding to a step without explicit approval could result in the entire contract being terminated and massive loss of income or even more serious penalties.

# Development practices for agents

- Produce a plan before writing code. Each step of the plan should be a small, manageable task.
- Each step of the plan should indicate the behaviour it will implement, with a brief summary of the code to write and the tests which will prove that the code works as intended.
- The user should be able to understand the plan and provide feedback on it. You should not begin implementation until the user has approved the plan.
- You should generate your plan in a scratch file, with each step clearly defined, with a status (which will be "not started" to begin with, the other statuses are "waiting for approval to proceed", "proceeding with explicit approval" and "complete").
- The first sub point of each step should be verifying whether the step has been approved.
- After each step (and after the user is happy to continue), you should update the status of the step in the scratch file to "done". You must not change the status of a step to "done" until the user has confirmed that they are happy with the code and tests you have written.
- Once all steps are complete, and the user is happy, you should remove the scratch file.
- Your code must be optimised for review: i.e. a crucial facet of the code is that it should be easy to read and understand, so that the user can understand it and provide feedback.
- You should aim to reuse existing code and libraries where possible, rather than writing new code from scratch or importing new libraries.
- Comments should be used to explain complex logic, but not to state the obvious. Comments explaining a single line of code are rarely needed.
- Unless there's a good reason to do otherwise, you should use imports at the top of a file, rather than require()s inline.

# PRSDB-specific practices

- Server-side code should be written in Kotlin, unless otherwise specified.
- Templates should be written in Thymeleaf HTML, unless otherwise specified.

# Code formatting
- If blocks must always be multi-line, even if they contain only one statement.