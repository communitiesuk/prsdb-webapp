# ADR-0042: Synchronous payment status checks vs webhooks

## Status

Proposed

Date of decision:

## Context and Problem Statement

We will use GOV.UK Pay with deferred payments, capturing payment as part of finalising the property registration.
Users may close their browser after confirming their payment on GOV.UK Pay, before returning to our service.
How should we check payment status, and when should we prioritise introducing webhooks?

## Considered Options

* Synchronous API calls only
* Webhooks only
* Synchronous API calls plus webhooks

## Decision Outcome

Synchronous API calls only initially, because it is the simplest option to implement. Post-MVP, we will add webhooks
alongside synchronous API calls so that registrations can be finalised even if the user does not return, without making
returning users wait for a webhook.

## Pros and Cons of the Options

### Synchronous API calls only

When the user returns from GOV.UK Pay, check that the payment is ready for capture, then finalise the property registration,
including capturing the payment.

* Good, because it is the simplest option to implement.
* Good, because users returning to the service do not need to wait for a webhook before their registration is finalised.
* Good, because there is a single route for triggering finalisation, rather than coordinating the return journey and a webhook.
* Bad, because if the user does not return to the service, their registration will not be finalised and they will have to
  go through the payment process again.
* Bad, because users who do not return to the service may believe they have paid and registered when they have not.

### Webhooks only

Receive a webhook from GOV.UK Pay indicating that the payment is ready for capture, then finalise the property registration,
including capturing the payment, without depending on the user returning to the service.

* Good, because the payment can be captured and the registration finalised even if the user does not return to the service.
* Good, because there is a single route for triggering finalisation, rather than coordinating the return journey and a webhook.
* Good, because GOV.UK Pay retries failed webhook deliveries.
* Bad, because users returning to the service may need to wait for the webhook to be processed before their registration is finalised.
* Bad, because webhook messages may arrive more than once or out of order, which finalisation must handle safely.
* Bad, because if webhook delivery still fails after GOV.UK Pay stops retrying, the registration will not be finalised automatically.
* Bad, because we must handle users retrying registration or payment before a webhook arrives, or after webhook delivery has failed.

### Synchronous API calls plus webhooks

Use both synchronous API calls on return from GOV.UK Pay and webhooks to identify when a payment is ready for capture.
Either route can trigger finalisation of the property registration, including capturing the payment.

* Good, because the payment can be captured and the registration finalised even if the user does not return to the service.
* Good, because users returning to the service do not need to wait for a webhook before their registration is finalised.
* Good, because GOV.UK Pay retries failed webhook deliveries.
* Good, because webhooks can be added without replacing the synchronous return journey.
* Bad, because it requires additional implementation to receive, verify and process webhook messages.
* Bad, because finalisation must safely handle either route running first, both routes running at once, and webhook retries
  without finalising the same registration twice.
* Bad, because webhook messages may arrive more than once or out of order, which finalisation must handle safely.
* Bad, because we must handle users retrying registration or payment before a webhook arrives, or after webhook delivery has failed.

## More Information

* https://docs.payments.service.gov.uk/delayed_capture/
* https://docs.payments.service.gov.uk/webhooks/
