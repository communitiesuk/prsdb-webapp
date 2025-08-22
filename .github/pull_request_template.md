## Ticket number

In the format of "PRSD-XXXX" so that github will auto-link to the jira ticket

## Goal of change

Summary of the problem the PR is trying to solve - usually a 1 sentence summary of the ticket

## Description of main change(s)

Summary of the changes made. These should focus on the functionality that you've changed rather than the actual code
changes - those will be clear from the PR.
E.g. Prefer "Adds new endpoint for uploading gas safety certificates to s3" to "Adds new `UploadGasSafetyCertificate`
controller that accepts a `UploadFileRequest` and uses the `fileUploadService` to upload the file to s3"

## Anything you'd like to highlight to the reviewer?

Include e.g. anything unusual about the PR, where there was some debate over how to implement it, or anywhere you were
unsure of the approach to take and would like specific feedback.

## Checklist

Delete any that are not applicable, and add explanation below for any that are applicable but haven't been done

- [ ] Screenshots of any UI changes have been added
- [ ] Unit tests for new logic (e.g. new service methods) have been added
- [ ] Controller tests for any new endpoints, including testing the relevant permissions
- [ ] Single page integration tests have been added for any unhappy-flow UI features, e.g. validation errors
- [ ] New journey steps have been added to the appropriate journey integration test(s)
- [ ] A new journey integration test has been added for any new journeys
- [ ] New email templates have been added to `/src/main/kotlin/resources/emails/emailTemplates.json`
- [ ] Test suite has been run in full locally and is passing
- [ ] Branch has been rebased onto main and run locally, with everything working as expected (both for your new feature
  and any related functionality)
- [ ] TODO comments referencing this JIRA ticket have been searched for and removed - if a future PR will address them,
  mention that here
- [ ] Seed data has been updated as needed for your feature to be tested without having to e.g. register a new property
- [ ] Any special release instructions (e.g. the database will need resetting) have been added as checklist items to a draft PR (merging `main` into `test`) for the next release
