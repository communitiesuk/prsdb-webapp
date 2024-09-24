# ADR-0009: User Message Sending

## Status

Accepted

Date of decision: 2024-08-23

## Context and Problem Statement

The Private Rented Sector Database (PRSDB) will need to send emails and possibly letters to users. CDDO guidance in line
with its strategic commitments is
to [use the GOV.UK Notify service](https://www.gov.uk/guidance/use-mandated-central-services) to send notifications to
users by email, post, and SMS.

Is Notify suitable?

## Considered Options

* GOV.UK Notify
* AWS SES, SendGrid, or other alternatives

## Decision Outcome

GOV.UK Notify, because it fulfils PRSDB’s needs, is in line with government guidance, and likely cheaper than commercial
alternatives.

## Pros and Cons of the Options

### GOV.UK Notify

GOV.UK Notify is a government service to “send emails, text messages and letters to your users.” For further
investigation into its capabilities, see Gov.uk Notify Review.docx

* Good, because it is in line with government guidance
* Good, because it supports sending simple, branded emails
* Good, because it can send letters if needed
* Good, because sending emails is free
* Neutral, because sending letters has a cost, but that cost is very competitive
* Neutral, because formatting is constrained, but those constraints can help produce consistently well-rendered results

### AWS SES, SendGrid, or other alternatives

There are various commercially available alternatives to Notify, such as AWS Simple Email Service, SendGrid, or
PostGrid. Their features and pricing vary and would need further investigation if chosen.

* Bad, because it is not in line with government guidance
* Bad, because there are few (or perhaps no) services that support both email and posted letters – multiple suppliers
  would likely be needed
* Bad, because costs are typically higher: emails would attract costs, and letters would likely cost more (e.g. PostGrid
  charges $0.92 = ~£0.72, vs Notify’s £0.61)

## More Information

* [Gov.uk Notify Review.docx](https://mhclg.sharepoint.com/:w:/s/PrivateRentedSector/EUTTLPGOQOpKm4sKoYelqsYB7XR8n5CaWPy_rCNaDdMg-w?e=tiMqoK)
* GOV.UK Notify homepage: https://www.notifications.service.gov.uk/
* GOV.UK Notify usage details: https://www.notifications.service.gov.uk/using-notify 