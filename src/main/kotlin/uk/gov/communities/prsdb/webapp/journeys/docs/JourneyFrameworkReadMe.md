# Journey Framework

The Journey Framework is designed to make creating complex multi-page form journeys quick and easy without compromising on flexibility.

## Overview

The framework aims to:
- Commonise behaviours of multi-page form journeys without making it difficult to implement custom requirements
- Allow pages and groups of pages to be reused across different journeys
- Separate the structure of journeys from the content to make both easier to maintain and update

## More Documentation
- [Understanding the Journey Framework Model](JourneyFrameworkModel.md)
- [Create a Journey](JourneyFrameworkCreateAJourney.md)
- [Extending the Journey Framework](JourneyFrameworkExtendTheFramework.md)

## Glossary of terms

| Term | Definition                                                                                                                                                                        |
|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Journey** | A complete multi-page form flow modelled as a directed graph, where users navigate through a series of pages to complete a task.                                                  |
| **Journey Element / Element** | A node in the journey graph. Examples include steps and tasks.                                                                                                                    |
| **Step** | The basic building block of a journey representing an individual page where users provide input or view information or a purely logical node of the journey.                      |
| **Task** | A reusable group of steps with its own internal structure. Acts as a "black box" within the larger journey with a common start point and exit point.                              |
| **Mode** | An enum value representing how a user's answer affects journey structure. Derived from the user's input and/or external factors. Returns `null` if the page has not been answered. |
| **Outcome** | Similar to mode but accounts for reachability — the mode value if the step is reachable, `null` otherwise.                                                                        |
| **Reachability** | Whether a step can be visited, determined by the completion status of parent steps.                                                                                               |
| **Parents** | Previous elements that must be completed for an element to be visitable. Defines graph structure and reachability rules.                                                          |
| **Journey State** | A snapshot of the user's progress through the journey, stored in the session between HTTP requests. Includes submitted form data and derived data needed for journey logic.       |
| **Journey ID** | An identifier for a journey instance, passed as a query parameter on each request.                                                                                                |
| **Form Model** | A class defining the data structure collected on a page, including primary validation rules for form fields.                                                                      |
| **Route Segment** | The URL path segment identifying a step (e.g., `"name"` in `/journey/name`).                                                                                                      |
| **Step Config** | A configuration class extending `AbstractStepConfig` that defines step-specific logic including template, content, and mode determination.                                        |
| **Step Lifecycle Orchestrator** | A class that manages the request lifecycle for a step, calling appropriate step functions for GET and POST requests.                                                              |
| **Savable Journey** | A journey whose state can be persisted to a database, allowing users to resume the journey in a later session.                                                                    |
| **Requestable Step** | A step that can be directly accessed via a URL, representing a page in the journey.                                                                                               |
| **Internal Step** | A step without an associated page that performs logic related to journey structure.                                                           |
