# ADR-0033: Feature Flags

## Status

Draft

Date of decision: Pending

## Context and Problem Statement
We would like to be able to deploy to production at any point, even if some features are incomplete or otherwise 
should not be made public - i.e. we would like to enable trunk-based development. 
We will use feature flags to hide incomplete/unwanted features in production.

Depending on how we implement them, feature flags could also provide a number of other benefits, such as allowing:
 * A/B testing
 * enabling/disabling features for specific users
 * gradually rolling out features
 * quickly disabling a feature if it is causing problems without needing to do a new deployment
 * testing features in an environment without exposing them to all users

What feature flag system should we use?

## Considered Options

* Simple if statements to check property status in functional code
* Beans selected by static configuration
* Static configuration with a library selecting beans
* Library selecting beans with database backing and console
* Feature Flags as a Service (FFaaS) product

## Decision Outcome
Beans selected by static configuration, because behaviour is tied to a deployment (making it predictable and 
repeatable), it is simple to implement, and it meets our (currently simple) needs.

## Pros and Cons of the Options

### Simple if statements in code

Directly branch code using if statements for feature flags.

* Good, because it is simple to implement and requires no additional dependencies.
* Bad, because it leads to messy code and makes feature flag logic hard to maintain and test.
* Bad, because it does not leverage our dependency injection system, reducing code clarity and maintainability.

### Beans selected by static configuration

Use static configuration to select which beans are active, controlling feature availability at startup.

* Good, because behaviour is fixed per deployment, ensuring predictability and replicability.
* Good, because it integrates well with our DI system and keeps code clean.
* Bad, because it does not support advanced features like A/B testing or gradual rollouts without custom code.
* Good, because any advanced features will be well understood by us.
* Neutral, as it requires a restart with reconfiguration to change flags, but this is acceptable for our use case.

### Static configuration with a library selecting beans

Use a feature flag library (e.g., Togglz, FF4J) to manage flags via static configuration, with DI integration.

* Good, because behaviour is fixed per deployment, ensuring predictability and replicability.
* Good, because it integrates well with our DI system and keeps code clean.
* Good, because it supports some advanced features using static context without custom code.
* Bad, because any advanced features not supported by the library would require integrated custom implementation.
* Neutral, as it requires a restart with reconfiguration to change flags, but this is acceptable for our use case.

### Library selecting beans with database backing and console

Use a feature flag library with database-backed flags and an admin console for runtime changes.

* Bad, because behaviour is not fixed per deployment, reducing predictability and replicability.
* Good, because it integrates well with our DI system and keeps code clean.
* Good, because it supports some advanced features without custom code.
* Bad, because any advanced features not supported by the library would require integrated custom implementation.
* Neutral, because it allows runtime flag changes, to alter the service behaviour without redeployment (but this is not needed).
* Bad, because it introduces an additional potential security concern and operational complexity.
* Bad, because inadvertent mistakes in the admin console could degrade service or expose features unintentionally.

### Feature Flags as a Service (FFaaS)

Use a commercial feature flag service (e.g., LaunchDarkly, Unleash).

* Bad, because behaviour is not fixed per deployment, reducing predictability and replicability.
* Bad, because it will not provide native DI integration with Spring, leading to additional complexity.
* Bad, because this would not provide implementations of advanced features out of the box, requiring custom integration.
* Bad, because it is expensive and provides advanced features like management across distributed systems that we do not need.
* Bad, because it introduces external dependencies and operational overhead.

## More Information

* https://www.togglz.org
* https://ff4j.github.io/
* https://martinfowler.com/articles/feature-toggles.html
