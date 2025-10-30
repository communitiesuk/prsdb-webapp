# ADR-0034: Plausible custom events

## Status

Accepted

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

We want to be able to generate a Sankey diagram for each of our journeys showing the paths that users take through the journey and where they are dropping out. By default, Plausible’s API & default events do not give us enough data at suitable granularity to generate these diagrams.

## Considered Options

* Event generation:
  * Front-end custom events.
  * Back-end custom events.
* Event data:
  * Using the referrer header.
  * Using Session ID.

## Decision Outcome

Front-end events using the referrer header, because this provides sufficient data (as demonstrated by a spike) and is the simplest technical change.

## Pros and Cons of the Options

### Event generation

#### Front-end custom events

Send events to Plausible (via JavaScript) on page load in the browser. Provide data to the browser via model attributes populated by a custom interceptor.

* Good, because the implementation is straightforward and has minimal impact on the rest of the webapp.
* Good, because the data is sent on page load, so there is no risk of missing data due to server-side issues.
* Bad, because it relies on the browser to send the data, which could be blocked by ad blockers or other privacy tools. However, this could be mitigated by proxying Plausible.
* Bad, because it relies on JavaScript being enabled in the browser.

#### Back-end custom events

Send events to Plausible from the server-side application, by integrating with the journey framework's handling of GET requests.

* Good, because it does not rely on the browser to send the data, so it is less likely to be blocked by ad blockers or other privacy tools.
* Good, because it does not rely on JavaScript being enabled in the browser.
* Bad, because it requires additional implementation work to integrate with the journey framework.

### Event data

#### Using the referrer header

Send a custom event to Plausible on each page load with the following:
1. `"Flow": {props: {"page-a", "page-b"}}`
2. Where `page-a` is the referrer and `page-b` is the current page.

* Good, because the referrer header is automatically set by the browser, so the implementation of this is straightforward.
* Good, because it is minimally intrusive to the user and would not require any additional cookies or tracking.
* Bad, because the referrer header is not always set (e.g. if the user navigates directly to a page, or if the user has certain privacy settings enabled).
* Bad, because it does not capture specific user journeys, only page-to-page transitions.
* Good, because even though it does not capture specific user journeys, it will still allow us to generate a Sankey diagram that shows the most common page-to-page transitions / user flows.


#### Using the Session ID

Send a custom event to Plausible on each page load with the following:
1. `"Flow": {props: {"page-a", "page-b", "session-id"}}`
2. Where `page-a` is the referrer, `page-b` is the current page, and `session-id` is a unique identifier for the user's session.
3. The session ID would be passed through a one-way hash function to ensure that it is not possible to identify the user.

* Good, because it would allow us to capture specific user journeys, rather than just page-to-page transitions.
* Good, because it would allow us to filter out duplicate page views within a session, which would give us a more accurate picture of user flows.
* Bad, because it could potentially raise privacy concerns, even if the session ID is hashed.
* Bad, because it would require additional implementation work to hash the session ID and manage the hashing process.
