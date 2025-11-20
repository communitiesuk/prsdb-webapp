# Feature Flags

We are implementing feature flags using [FF4j](https://ff4j.github.io/).

We are setting the flags statically - their values will be set in the codebase.

We are using an Aspect Oriented Programming (AOP) approach, using annotations to switch behavior based on the value of a feature flag.

Flags should have expiry dates to prevent unintentionally leaving feature flags in our code for a long time.
We are not stopping the flags from working when they expire, but we do have a test that checks whether each feature has a flag and whether it is in date.

Feature flags can (optionally) be assigned to a group (each flag can only be in one group though).
The "enabled" boolean on the group overrides the individual flag settings, allowing all features in the group to be switched on or off by changing one setting.
This is most likely to be used for grouping features into a particular release.

## Adding/Modifying Feature Flags

Feature flags are defined in the `FeatureFlagConfig.kt` file.
Individual flags are set and enabled by adding a new `FeatureFlagModel` to the list in the `featureFlags` val.
The expiry date for the flag is set in the `FeatureFlagModel`.
Flags can (optionally) be assigned to a group setting `flagGroup` in the `FeatureFlagModel` to the group name.

To define a new group, add a new `FeatureFlagGroupModel` to the `featureGroups` list in `FeatureFlagConfig`.

(TODO PRSD-1647 - add information on flipping strategies here if we use them)

## Feature flagged services

You can define a service which calls different versions of a function depending on the value of a feature flag.

* Define an interface (see `ExampleFeatureFlaggedService.kt`)
* Define two implementations of the interface, annotated with `@PrsdbWebService("bean-name")`.
    * Add the `@Primary` annotation to the implementation that should be used be default (see `ExampleFeatureFlagServiceImplFlagOff`).
* Annotate members in your interface with `@PrsdbFlip(name = "...", alterBean = "...")` where the `alterBean` value matches the name you gave to your second implementation (see `ExampleFeatureFlagServiceImplFlagOn.kt`)`.

To use your feature flagged service, pass in the interface (see `ExampleFeatureFlagTestControlle`r) - it will automatically call the correct implementation based on the feature flag value.

## Feature flagged endpoints
To make an endpoint available only when a feature is enabled, annotate it with `@AvailableWhenFeatureEnabled("flag-name")`

To make an endpoint available only when a feature is disabled, annotate it with `@AvailableWhenFeatureDisabled("flag-name")`

(See examples in `ExampleFeatureFlagTestController`)

Currently, we enforce that only one of these annotations can be used on a given endpoint (with the `FeatureFlagAnnotationValidator`).


### Implementation notes
* Added two annotations that can be applied to endpoints: `AvailableWhenFeatureEnabled` and `AvailableWhenFeatureDisabled`.
    * This allows us to switch _off_ a particular endpoint (such as a placeholder) when a feature is enabled, as well as switching endpoints on.
    * Only one can be applied to a given endpoint (enforced by `FeatureFlagAnnotationValidator`)`.
    * If we decide to allow both annotations on the same endpoint in future, we should update `FeatureFlagHandlerMapping` to disable the endpoint if required by either flag.
* Added a request condition to be used with each annotation.
    * Currently only implemented `getMatchingCondition`
    * There are combine and compareTo methods which may be useful for combining or prioritizing multiple conditions if we need to do that in future.
* `FeatureFlagConditionMapping` - this checks every endpoint in the codebase, and applies the relevant request condition if one of the feature flag annotations is present.


## Feature flag group demo
The enabled/disabled value of individual flags is effectively overridden by the group setting if the flag is in a group.
`EXAMPLE_FEATURE_FLAG_TWO` and `EXAMPLE_FEATURE_FLAG_THREE` have been added to the `RELEASE_1_0` group.

The group behaviour is demonstrated by a set of endpoints in `ExampleFeatureFlagTestController` (which expose the value set by developers in config to the user)
* `/feature-flagged-endpoint-test/grouped-features/example-feature-flag-two`
  * Available when the `EXAMPLE_FEATURE_FLAG_TWO` feature is enabled
* `/inverse-feature-flagged-endpoint-test/grouped-features/example-feature-flag-two`
  * Available when the `EXAMPLE_FEATURE_FLAG_TWO` feature is disabled
* `/feature-flagged-endpoint-test/grouped-features/example-feature-flag-three`
  * Available when the `EXAMPLE_FEATURE_FLAG_THREE` feature is enabled
* `/inverse-feature-flagged-endpoint-test/grouped-features/example-feature-flag-three`
  * Available when the `EXAMPLE_FEATURE_FLAG_THREE` feature is disabled

For a useful demo, check that in `featureFlags`
* `EXAMPLE_FEATURE_FLAG_TWO` is set to enabled = true
* `EXAMPLE_FEATURE_FLAG_THREE` is set to enabled = false

Then toggle the `RELEASE_1_0` group enabled setting to see the endpoints become available or unavailable as appropriate.

## Tests
Tests should inherit from FeatureFlagTest. This uses the real FeatureFlagConfig to get flag values, but they can be enabled or disabled in particular tests as required.

See the following for example tests:
* ExampleFeatureFlagServiceTest.kt
* ExampleFeatureFlaggedEndpointAvailabilityTest.kt

### Related tests
We can add controller tests in the usual way - the endpoints are called whether the feature is enabled or not because @WebMvcTest doesn't check the WebMvcRegistrations
* ExampleFeatureFlagTestControllerTests.kt
