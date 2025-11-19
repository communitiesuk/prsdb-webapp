# Feature Flags

We are implementing feature flags using [FF4j](https://ff4j.github.io/).

We are setting the flags statically - their values will be set in the codebase.

We are using an Aspect Oriented Programming (AOP) approach, using annotations to switch behavior based on the value of a feature flag.

Flags should have expiry dates to prevent unintentionally leaving feature flags in our code for a long time.
We are not stopping the flags from working when they expire, but we do have a test that checks whether each feature has a flag and whether it is in date.

## Adding/Modifying Feature Flags

Feature flags are defined in the FeatureFlagConfig.kt file. Individual flags are set and enabled in the featureFlags val.

(TODO PRSD-1647 - add information on Feature Flag groups and maybe flipping strategies here if we use them)

## Feature flagged services

You can define a service which calls different versions of a function depending on the value of a feature flag.

* Define an interface (see ExampleFeatureFlaggedService.kt)
* Define two implementations of the interface, annotated with @PrsdbWebService("bean-name").
    * Add the @Primary annotation to the implementation that should be used be default (see ExampleFeatureFlagServiceImplFlagOff).
* Annotate members in your interface with @PrsdbFlip(name = "...", alterBean = "...") where the alterBean value matches the name you gave to your second implementation (see ExampleFeatureFlagServiceImplFlagOn.kt).

To use your feature flagged service, pass in the interface (see ExampleFeatureFlagTestController) - it will automatically call the correct implementation based on the feature flag value.

## Feature flagged endpoints
To make an endpoint available only when a feature is enabled, annotate it with @AvailableWhenFeatureFlagEnabled("flag-name")

To make an endpoint available only when a feature is disabled, annotate it with @AvailableWhenFeatureFlagDisabled("flag-name")

(See examples in ExampleFeatureFlagTestController)

Currently, we enforce that only one of these annotations can be used on a given endpoint (with the FeatureFlagAnnotationValidator).


### Implementation notes
* Added two annotations that can be applied to endpoints: AvailableWhenFeatureFlagEnabled and AvailableWhenFeatureFlagDisabled.
    * This allows us to switch _off_ a particular endpoint (such as a placeholder) when a feature is enabled, as well as switching endpoints on.
    * Only one can be applied to a given endpoint (enforced by FeatureFlagAnnotationValidator).
    * If we decide to allow both annotations on the same endpoint in future, we should update FeatureFlagHandlerMapping to disable the endpoint if required by either flag.
* Added a request condition to be used with each annotation.
    * Currently only implemented getMatchingCondition
    * There are combine and compareTo methods which may be useful for combining or prioritizing multiple conditions if we need to do that in future.
* FeatureFlagConditionMapping - this checks every endpoint in the codebase, and applies the relevant request condition if one of the feature flag annotations is present.


## Feature flag groups
Feature flags can be assigned to a group (each flag can only be in one group though). This may be useful for grouping features into a particular release.

## Tests
Tests should inherit from FeatureFlagTest. This uses the real FeatureFlagConfig to get flag values, but they can be enabled or disabled in particular tests as required.

See the following for example tests:
* ExampleFeatureFlagServiceTest.kt
* ExampleFeatureFlaggedEndpointAvailabilityTest.kt

### Related tests
We can add controller tests in the usual way - the endpoints are called whether the feature is enabled or not because @WebMvcTest doesn't check the WebMvcRegistrations
* ExampleFeatureFlagTestControllerTests.kt
