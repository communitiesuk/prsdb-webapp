# ADR-0033: plausible custom events

## Status

{Draft ~~| Proposed | Accepted | Rejected | Superseded~~}

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

We want to be able to generate a Sankey diagram for each of our journeys showing the paths that users take through the journey and where they are dropping out. By default, Plausible’s API & default events do not give us enough data at suitable granularity to generate these diagrams.

## Considered Options

* Front-end custom events.
* Back-end custom events.
* Using the referrer header.
* Using Session ID.



## Decision Outcome

The chosen option is Front-end events with the referrer header, because it is the least intrusive to users while still providing useful data for generating Sankey diagrams. As part of the research into the options this quickly became the simplest and easiest of the options. It was clear that sending just the referrer header and current url as an event would create sufficient data to generate useful Sankey diagrams. The Front end was chosen as the delivery method as it was the simplest to implement with minimal impact on the existing webapp.
There is an existing prototype on branch `PRSD-1610/Front-end-referrer-header`, this has allowed me to implement this method and generate some mock data using plausible. I have then used the data to create a Sankey diagram using draxlr.com which has demonstrated that this method will work.

A tool will need to be created to generate the Sankey diagrams in a more useful format, this has not been implemented as part of the ADR but my suggestion based on the research I have done is to use D3.js with d3-sankey. This will allow for sufficient customisation to create useful diagrams for analysis.

## Pros and Cons of the Options

### Front-end VS Back-end custom events

#### Front-end custom events

Sending the plausible event from the front end (eg, on page load). The suggested method for this is to create an interceptor that will provide the model attributes for the plausible event properties and then these would be sent from the browser on page load.

* Good, because the implementation is straightforward and has minimal impact on the rest of the webapp.
* Good, because the data is sent on page load, so there is no risk of missing data due to server-side issues.
* Bad, because it relies on the browser to send the data, which could be blocked by ad blockers or other privacy tools.
* Bad, because it relies on JavaScript being enabled in the browser.

#### Back-end custom events

Sending the plausible event from the back end (eg, as part of the journey framework). The suggested method for this is to create a service that will provide the model attributes for the plausible event properties and then these would be sent from the server on page load.

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


## More Information

- In relation to the generation of Sankey diagrams there will need to be a list of `nodes` which will represent the endpoints in the journey. d3 can then be used to determine the order of the nodes. This is important for readability as with the complexity of the journeys it quickly becomes unreadable without. In addition to this it is worth noting that you can align nodes to occupy the same horizontal space e.g. where several endpoints are part of the same step (such as licence type in the property registration journey)
