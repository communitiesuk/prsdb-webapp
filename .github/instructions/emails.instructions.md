---
applyTo: "src/main/resources/emails/**,**/models/viewModels/emailModels/**,src/test/kotlin/**/notify/**,**/testHelpers/EmailTemplateMetadata.kt"
---

# Email Template Instructions

## Documentation Reference
- [NotifyEmailsReadMe](../../docs/NotifyEmailsReadMe.md) describes Notify accounts, template versioning and local setup.
- Follow [services.instructions.md](services.instructions.md) for sending logic and transaction boundaries.

## Template Contract

Keep these parts consistent when adding or changing an email:

| Part | Location / contract |
|------|---------------------|
| Body | Markdown in `src/main/resources/emails/` using Notify syntax, not Thymeleaf |
| Metadata | `emailTemplates.json`: `enumName`, `subject`, `bodyLocation`, `test_id` and `prod_id` |
| Template identifier | `EmailTemplate` enum in `models/viewModels/emailModels/EmailTemplateModel.kt` |
| Personalization | An `EmailTemplateModel` implementation selecting `template` and returning `toHashMap()` |

Use the existing naming family for the model, Markdown file and enum entry. `bodyLocation` is a classpath resource
path such as `/emails/DelegateToLettingAgentInvitation.md`; the JSON `enumName` must match the enum entry exactly.

## Personalization

Notify placeholders use `((key))`. Map keys must match exactly, including spaces, and must cover both the body
and subject. They are not YAML message keys or Kotlin property names.

For example, `DelegateToLettingAgentInvitationEmail` maps `landlordName` to `"landlord name"` and
`singleLineAddress` to `"single line address"`. The latter appears in the JSON subject:

```text
You can provide details for ((single line address))
```

Reuse an existing model's `EmailTemplateModel` / `HashMap<String, String>` pattern. Keep message-specific
formatting in the appropriate model/helper and sending behaviour in services. Do not put credentials in
templates, metadata or committed configuration; Notify template IDs are metadata, not API keys.

## Updating Notify

Source changes alone do not update Notify. Each template needs matching copies in Notify-integration and Notify-prod:

- `test_id` identifies the integration/test template; `prod_id` identifies the production template.
- When changing an existing template, create a new versioned template in Notify rather than editing the live
  version in place. Update the source copy and both IDs together.
- Preserve old remote versions until promotion and the documented cleanup process allow removal. Editing them
  in place can break contract checks on branches still using the previous version.
- If remote template work is outstanding, state it explicitly; do not invent IDs or claim it has been completed.

## Local Sending and Tests

Local sending is stubbed by default. The stub uses `local & !use-notify`; adding `use-notify` opts into real Notify
with locally supplied credentials. A documentation or template edit is not permission to send live emails.

Add representative models to `EmailTemplateModelsTests.templateList()`. Its offline check compares each
`toHashMap()` with the combined subject/body placeholders. Extra keys require the fixture's explicit
`allowExtraKeys` option; do not enable that merely to hide an accidental key mismatch.

`NotifyEmailTemplateTests` separately compares source subjects/bodies and IDs with the remote integration and
production templates. These checks are not ordinary offline tests:

- Integration uses `EMAILNOTIFICATIONS_APIKEY`; production uses `EMAILNOTIFICATIONS_PRODUCTION_APIKEY`.
- Remote checks skip an environment without its key; the class is disabled when neither key is configured.
- Keep tests and model factories aligned with enum/placeholder changes. Follow the project's test approval rules.
