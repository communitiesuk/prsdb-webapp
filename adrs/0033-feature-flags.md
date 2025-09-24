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
* If statements with hard-coded switch values
* If statements with custom interface
* Spring Beans natively selected by per-environment configuration
* Library selecting beans by per-environment configuration with additional overrides
* Library selecting beans with self-hosted dynamic flags
* Feature Flags as a Service (FFaaS) product

## Decision Outcome
Library selecting beans by per environment static configuration with additional overrides, because it provides a good balance of simplicity,
maintainability, and potential for future expansion if needed.
Beans selected by static configuration, because behaviour is tied to a deployment (making it predictable and
repeatable), it is simple to implement, and it meets our (currently simple) needs.

## Pros and Cons of the Options

### If statements with hard-coded switch values

Directly branch code using if statements decision points and have all feature flag values hard-coded as constants.

* Good, because it is simple to implement and requires no additional dependencies.
* Bad, because it leads to messy code and makes feature flag logic hard to maintain and test.
* Bad, because it does not leverage our dependency injection system, reducing code clarity and maintainability.
* Bad, because it does not allow different values in different environments with the same version of the code.

### If statements with custom interface

Directly branch code using if statements decision points and have all feature flag values calculated based
on environment values, but still hard-coded.

* Good, because it is relatively simple to implement and requires no additional dependencies.
* Bad, because it leads to messy code and makes feature flag logic hard to maintain and test.
* Bad, because it does not leverage our dependency injection system, reducing code clarity and maintainability.
* Good, because it allows different values in different environments with the same version of the code.
* Bad, because feature flag values cannot be changed without changing the code.

### Spring Beans natively selected by per-environment configuration

Use per-environment configuration to select which beans are active, controlling feature availability at startup. The beans can either be
alternative implementations of the same interface, or an interface determining whether a feature is enabled.

* Good, because behaviour is fixed per deployment to a given environment, ensuring predictability and replicability of behaviour.
* Good, because it integrates well with our DI system and keeps code clean.
* Bad, because it does not support advanced features like A/B testing or gradual rollouts without custom code.
* Good, because any advanced features implemented will be well understood by us.
* Neutral, as it requires a restart with reconfiguration to change flags, but this is acceptable for our use case.

### Library selecting beans by per-environment configuration with additional overrides

Use a feature flag library (e.g., Togglz, FF4J) to manage flags via per-environment configuration, with DI integration. This gives all the
advantages of the previous option, but with some additional features provided by the library such as targeting specific users.

* Good, because behaviour is fixed per deployment, ensuring predictability and replicability.
* Good, because it integrates well with our DI system and keeps code clean.
* Good, because it supports some advanced features using static context without custom code.
* Bad, because any advanced features not supported by the library would require integrated custom implementation.
* Neutral, as it requires a restart with reconfiguration to change flags, but this is acceptable for our use case.
* Good, because it will support extension to dynamic flags in future if needed.

### Library selecting beans with self-hosted dynamic flags

Use a feature flag library as above with per-environment defaults and database-backed flags and an admin console for runtime changes.

* Bad, because behaviour is not fixed per deployment, reducing predictability and replicability.
* Good, because it allows runtime flag changes, to alter the service behaviour without redeployment.
* Good, because it integrates well with our DI system and keeps code clean.
* Good, because it supports some advanced features without custom code.
* Bad, because any advanced features not supported by the library would require integrated custom implementation.
* Bad, because it introduces an additional potential security concern and operational complexity.
* Bad, because inadvertent mistakes in the admin console could degrade service or expose features unintentionally.
* Bad, because implementation will be more involved and include infrastructure changes.

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
