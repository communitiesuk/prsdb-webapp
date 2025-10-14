# ADR-0033: plausible custom events

## Status

{Draft ~~| Proposed | Accepted | Rejected | Superseded~~}

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

Is it possible to use Plausible Custom Events to gather enough information about user interactions on the site to generate a Sankey diagram?

## Considered Options

* Front-end custom events.
* Back-end custom events.
* Using the referrer header.
* Using Session ID.



## Decision Outcome

{Title of Option X}, because {summary justification / rationale}.

## Pros and Cons of the Options

### Front-end VS Back-end custom events

#### Front-end custom events

{Description of the option}

* {Good | Bad | Neutral}, because {argument a}.
* {Good | Bad | Neutral}, because {argument b}.
* {... etc ...}

#### Back-end custom events

{Description of the option}

* {Good | Bad | Neutral}, because {argument a}.
* {Good | Bad | Neutral}, because {argument b}.
* {... etc ...}

### Referrer header VS Session ID

#### Using the referrer header

Send a custom event to Plausible on each page load with the following:
1. `"props": {"flow": "page-a", "page-b"}`
2. Where `page-a` is the referrer and `page-b` is the current page.

* Good, because the referrer header is automatically set by the browser, so the implementation of this is straightforward.
* Good, because it is minimally intrusive to the user and would not require any additional cookies or tracking.
* Bad, because the referrer header is not always set (e.g. if the user navigates directly to a page, or if the user has certain privacy settings enabled).
* Bad, because it does not capture specific user journeys, only page-to-page transitions.
* Good, because even though it does not capture specific user journeys, it will still allow us to generate a Sankey diagram that shows the most common page-to-page transitions / user flows.
* {... etc ...}

#### Using the Session ID

Send a custom event to Plausible on each page load with the following:
1. `"props": {"flow": "page-a", "page-b", "session-id"}`
2. Where `page-a` is the referrer, `page-b` is the current page, and `session-id` is a unique identifier for the user's session.
3. The session ID would be passed through a one-way hash function to ensure that it is not possible to identify the user.

* Good, because it would allow us to capture specific user journeys, rather than just page-to-page transitions.
* Good, because it would allow us to filter out duplicate page views within a session, which would give us a more accurate picture of user flows.
* Bad, because it would require setting a cookie or using some other method to track the user's session, which could be seen as intrusive.
* Bad, because it would require additional implementation work to generate and manage the session IDs.
* Bad, because it could potentially raise privacy concerns, even if the session ID is hashed.
* Bad, because it would require additional implementation work to hash the session ID and manage the hashing process.
* {... etc ...}

## More Information

{Optionally, any supporting links or additional evidence}
