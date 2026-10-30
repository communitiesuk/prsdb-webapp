# Notify
Notify is the government notification service that we use to send all emails and letters.
We have a Notify-integration account that sends the emails for the `integration` and `test` environments.
We have a Notify-prod account that sends the emails for the `production` environment.
You should register for Notify-prod using your MHCLG email address.

Note: When running the app locally we stub the calls to notify by default.

## Emails
### Creating emails
When creating a new email you should do so in a markdown file in `src/main/resources/emails`.
You will also need to update `emailTemplates.json` with information about the new emails including the `test_id` and `prod_id` from the notify portal.

### Creating templates on notify
For each email we create in the codebase we also need to create a template on our Notify-integration and Notify-prod accounts.
Once you have created the templates on notify, also in markdown, you will be able to copy the `template ids`.
If you have Notify-integration and Notify-prod on the same account you can copy an email template from one to the other.
You should add the template ids to `emailTemplates.json` where:
- Template id from Notify-integration -> `test_id`
- Template id from Notify-prod -> `prod_id`

Note: If you don't have access to either of the notify accounts someone on the team should be able to invite you.

#### Updating templates
When updating any email templates you should create a new template with an increased version number on notify instead of editing the existing one.
This is to prevent the notify tests failing for all other branches until your changes have been promoted all the way to production.
You can still edit the existing email template in the codebase.
Update `emailTemplates.json` with the new `test_id` and `prod_id`.

##### Removing old templates
To prevent an accumulation of old templates on notify we should remove old templates once the new ones have been promoted to integration and then production.
To do this you should create a ticket and assign it to 2 sprints ahead of the current one, listing the specific templates to be removed from Notify-integration.
We do not yet have a way of tracking when old templates should be removed from Notify-prod, we should create one when we start regularly pushing to production.

### Receiving emails from Notify - Integration
On the `integration` and `test` environments in order to receive any emails each email needs to be registered as a user on the Notify-integration account.
If you are an admin user on notify you can invite others to register.

### Sending emails Locally
In order to send emails when you're running the app locally you need to use the correct profile and api key.

You need to add the `use-notify` profile to the ‘local’ springboot configuration.
You can do this by editing the configuration of ‘local’ and adding `use-notify` to the `Active profiles` field.

In your `.env` file you should add the Notify - Integration API Key instead as the `EMAILNOTIFICATIONS_APIKEY` variable.

Then when you run the app locally the emails will be sent when they are triggered.

### Testing

#### Unit tests
We have unit tests for the emails in `EmailTemplateModelsTests`.

#### Testing against notify templates
The testing for emails checks that the templates in notify match the templates in the codebase.
Every template is checked against both Notify-integration and Notify-prod, so a template that has only been updated in one of them is caught before it reaches production.
The tests are built into our pipelines so run whenever a PR is made.

Each notify service needs its own api key:
- Notify-integration -> `EMAILNOTIFICATIONS_APIKEY`
- Notify-prod -> `EMAILNOTIFICATIONS_PRODUCTION_APIKEY`

A service whose key is not set is skipped, and the tests are disabled entirely when neither key is set.

If you want to run them locally you need to add the `notify-template-tests.run.xml` to `.run` directory in the codebase. If you don't have a copy someone on the team should be able to help.
This will give you the test configuration to run the tests locally.

## Letters
Notify can also send letters by post. Letters use the same Notify accounts and api keys as emails.

### Creating letters
Letter templates follow the same process as emails, but live in `src/main/resources/letters`:
- Add the template body as a markdown file and an entry in `letterTemplates.json` with the `test_id` and `prod_id` from Notify.
  For letters, `subject` in `letterTemplates.json` is the letter heading.
- Add a value to the `LetterTemplate` enum and a model implementing `LetterTemplateModel`.
- Add the model to `templateList` in `LetterTemplateModelsTests`.

`LetterTemplateModelsTests` checks each letter model in its `templateList` matches its markdown template and doesn't use personalisation keys reserved for the recipient address (such as `address_line_1` or `postcode`), and `NotifyLetterTemplateTests` checks the letter templates against Notify in the same way as the email template tests, using the same api keys.

### Sending letters
Use `LetterNotificationService.sendLetter(recipientAddress, letter)`, where `recipientAddress` is created with `LetterAddressDataModel(lines)`.
- An address must follow [Notify's address rules](https://docs.notifications.service.gov.uk/java.html#send-a-letter):
  - The address must have at least 3 lines.
  - Address must be no more than 7 lines.
  - The first 2 lines of the address must include alphanumeric characters.
  - The last line needs to be a real UK postcode or the name of a country outside the UK.
    Notify checks for international addresses and will automatically charge the correct postage.
    A country is only accepted if sending letters to international addresses is turned on in the Notify service's settings;
    otherwise Notify rejects the address with "Must be a real UK postcode".
  - Address lines must not start with any of the following characters: `@ ( ) = [ ] " \ / , < >`
    ([Notify's validation](https://github.com/alphagov/notifications-utils/blob/main/notifications_utils/recipient_validation/postal_address.py#L33) also rejects `~`, although its docs do not list it).

  Notify rejects addresses that break these rules, and `sendLetter` throws a `PersistentLetterSendException`.
- Letter personalisation must not use the address keys (`address_line_1` to `address_line_7` or `postcode`), as these are set from the recipient address.
  Like Notify, this check ignores case, spaces, underscores and hyphens, so keys such as `Post Code` are also rejected.
- A successful send means Notify has accepted the letter, not that it has been delivered.
- Letters are not retried automatically. Retrying after a failure may send a duplicate letter, so check Notify before retrying a `PersistentLetterSendException` whose outcome is unknown.

### Sending letters locally
Letters are stubbed locally in the same way as emails, and are printed to the console instead.
Letters whose last address line matches a postcode in `local.letters` in `application-local.yml` always fail, so error handling can be tested.

To send letters through Notify locally, use the `use-notify` profile as described in [Sending emails Locally](#sending-emails-locally).
Letters use the same `EMAILNOTIFICATIONS_APIKEY` as emails, but whether a letter can be sent depends on the
[type of api key](https://docs.notifications.service.gov.uk/java.html#api-keys):
- **Test** - Notify accepts the letter and shows it in the dashboard, but never prints or posts it. Use this type to try letters locally.
- **Team and guest list** - Notify rejects the letter, as letters cannot be restricted to team members. If your key is this type, you need a test key to send letters.
- **Live** - Notify prints and posts the letter. Do not use a live key locally.

You can check a key's type on the API keys page of the Notify service.
