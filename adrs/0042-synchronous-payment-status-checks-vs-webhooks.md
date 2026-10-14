# ADR-0042: Synchronous payment status checks vs webhooks

## Status

Proposed

Date of decision:

## Context and Problem Statement

We need to confirm payment status to finalise property registrations, including when users do not return from GOV.UK Pay.
GOV.UK Pay supports deferred capture but does not send webhook events when a payment becomes ready for capture.
How should we check payment status, and when should we introduce background checks or webhooks?

## Considered Options

* Synchronous API calls only
* Webhooks only
* Synchronous API calls plus webhooks
* Synchronous API calls plus periodic checks

## Decision Outcome

Synchronous API calls only initially, because it is the simplest option to implement. Post-MVP, we will add periodic checks
alongside synchronous API calls so that registrations can be finalised even if the user does not return, while retaining
deferred capture.

## Pros and Cons of the Options

### Synchronous API calls only

When the user returns from GOV.UK Pay, check that the payment is ready for capture, then finalise the property registration,
including capturing the payment.

* Good, because it is the simplest option to implement.
* Good, because it supports deferred capture, allowing payment to be captured as part of registration finalisation.
* Good, because users returning to the service do not need to wait for a webhook before their registration is finalised.
* Good, because there is a single route for triggering finalisation, rather than coordinating the return journey and a webhook.
* Bad, because if the user does not return to the service, their registration will not be finalised and they will have to
  go through the payment process again.
* Bad, because users who do not return to the service may believe they have paid and registered when they have not.

### Webhooks only

Take payment without deferred capture, having ensured beforehand that the property record can be created.
Finalise registration when a webhook confirms payment has been taken, and unwind the preparatory changes if payment fails.

* Good, because the registration can be finalised even if the user does not return to the service.
* Good, because there is a single route for triggering finalisation, rather than coordinating the return journey and a webhook.
* Good, because GOV.UK Pay retries failed webhook deliveries.
* Bad, because we must ensure the property record can be created before taking payment, and unwind the preparatory changes
  if payment fails.
* Bad, because users returning to the service may need to wait for the webhook to be processed before their registration is finalised.
* Bad, because webhook messages may arrive more than once or out of order, which finalisation must handle safely.
* Bad, because if webhook delivery still fails after GOV.UK Pay stops retrying, payment may have been taken without the
  registration being finalised.
* Bad, because we must handle users retrying registration or payment before a webhook arrives, or after webhook delivery has failed.

### Synchronous API calls plus webhooks

Take payment without deferred capture, having ensured beforehand that the property record can be created.
Finalise registration when either a synchronous check on return or a webhook confirms payment has been taken.
Unwind the preparatory changes if payment fails.

* Good, because the registration can be finalised even if the user does not return to the service.
* Good, because users returning to the service do not need to wait for a webhook before their registration is finalised.
* Good, because GOV.UK Pay retries failed webhook deliveries.
* Bad, because introducing webhooks would require changing the initial deferred-payment flow.
* Bad, because it requires additional implementation to receive, verify and process webhook messages.
* Bad, because we must ensure the property record can be created before taking payment, and unwind the preparatory changes
  if payment fails.
* Bad, because payment may have been taken without the registration being finalised if the user does not return and webhook
  delivery ultimately fails.
* Bad, because finalisation must safely handle either route running first, both routes running at once, and webhook retries
  without finalising the same registration twice.
* Bad, because webhook messages may arrive more than once or out of order, which finalisation must handle safely.
* Bad, because we must handle users retrying registration or payment before a webhook arrives, or after webhook delivery has failed.

### Synchronous API calls plus periodic checks

Use deferred payments, checking their status when users return and periodically checking pending payments in the background.
Either route can finalise the property registration, including capturing the payment, once it is ready for capture.

* Good, because it supports deferred capture, allowing payment to be captured as part of registration finalisation.
* Good, because the payment can be captured and the registration finalised even if the user does not return to the service.
* Good, because users returning to the service do not need to wait for a periodic check before their registration is finalised.
* Good, because periodic checks can be added without changing the initial deferred-payment flow.
* Bad, because registrations for users who do not return will not be finalised until a periodic check runs.
* Bad, because we must trade off finalisation latency against the API calls and resource usage of more frequent checks.
* Bad, because finalisation must safely handle either route running first, both routes running at once, and repeated periodic
  checks without finalising the same registration twice.
* Bad, because it requires additional implementation to schedule and run the checks.
* Bad, because we must handle users retrying registration or payment before a periodic check finalises the registration.

## More Information

* https://docs.payments.service.gov.uk/delayed_capture/
* https://docs.payments.service.gov.uk/webhooks/
