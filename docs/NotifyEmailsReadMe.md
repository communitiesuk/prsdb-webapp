# Notify
Notify is the government email sending service that we use to send all emails.
We have a Notify-integration account that sends the emails for the `integration` and `test` environments.
We have a Notify-prod account that sends the emails for the `production` environment.
You should register for Notify-prod using your MHCLG email address.

Note: When running the app locally we stub the calls to notify by default.

## Creating emails
When creating a new email you should do so in a markdown file in `src/main/resources/emails`.
You will also need to update `emailTemplates.json` with information about the new emails including the `test_id` and `prod_id` from the notify portal. 

## Creating templates on notify
For each email we create in the codebase we also need to create a template on our Notify-integration and Notify-prod accounts.
Once you have created the templates on notify, also in markdown, you will be able to copy the `template ids`.
If you have Notify-integration and Notify-prod on the same account you can copy an email template from one to the other.
You should add the template ids to `emailTemplates.json` where:
- Template id from Notify-integration -> `test_id`
- Template id from Notify-prod -> `prod_id`

Note: If you don't have access to either of the notify accounts someone on the team should be able to invite you.

### Updating templates
When updating any email templates you should create a new template with an increased version number on notify instead of editing the existing one.
This is to prevent the notify tests failing for all other branches until your changes have been promoted all the way to production.
You can still edit the existing email template in the codebase. 
Update `emailTemplates.json` with the new `test_id` and `prod_id`.

#### Removing old templates
To prevent an accumulation of old templates on notify we should remove old templates once the new ones have been promoted to integration and then production.
To do this you should create a ticket and assign it to 2 sprints ahead of the current one, listing the specific templates to be removed from Notify-integration.
We do not yet have a way of tracking when old templates should be removed from Notify-prod, we should create one when we start regularly pushing to production.

## Receiving emails from Notify - Integration
On the `integration` and `test` environments in order to receive any emails each email needs to be registered as a user on the Notify-integration account.
If you are an admin user on notify you can invite others to register.

## Sending emails Locally
In order to send emails when you're running the app locally you need to use the correct profile and api key.

You need to add the `use-notify` profile to the ‘local’ springboot configuration.
You can do this by editing the configuration of ‘local’ and adding `use-notify` to the `Active profiles` field.

In your `.env` file you should add the Notify - Integration API Key instead as the `NOTIFY_APIKEY` variable.

Then when you run the app locally the emails will be sent when they are triggered.

## Testing

### Unit tests
We have unit tests for the emails in `EmailTemplateModelsTests`.

### Testing against notify templates
The testing for emails checks that the templates in notify match the templates in the codebase.
The test are built into our pipelines so run whenever a PR is made.

If you want to run them locally you need to add the `notify-template-tests.run.xml` to `.run` directory in the codebase. If you don't have a copy someone on the team should be able to help.
This will give you the test configuration to run the tests locally.
