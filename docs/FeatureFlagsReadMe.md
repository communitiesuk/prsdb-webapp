# Feature Flags

We are implementing feature flags using [FF4j](https://ff4j.github.io/).

We are setting the flags statically - their values will be set in the codebase.

We are using an Aspect Oriented Programming (AOP) approach, using annotations to switch behavior based on the value of a feature flag.

Flags should have expiry dates to prevent unintentionally leaving feature flags in our code for a long time.
We are not stopping the flags from working when they expire, but we do have a test that checks whether each feature has a flag and whether it is in date.

Feature flags can (optionally) be assigned to a release (each flag can only be in one release. This is using FF4J's Feature Group).
The "enabled" boolean on the release overrides the individual flag settings, allowing all features in the release to be switched on or off by changing one setting.

We can also add flipping strategies to feature flags and releases if required. If a strategy config is added to a release, this will override any strategy config on individual flags in that release.
Currently, we are set up to add ReleaseDateFlipStrategy (feature disabled until the release date), and a boolean flipping strategy (mainly added to check that strategies could be combined)

## Feature flag configuration

Default feature flags and releases  are defined in `application.yml` under `features`.

These can be overridden in `application-<environment-name>.yml` files as required.

Note each `release` must be added to at least one `feature-flag` as empty releases are not allowed.

In order to refer to feature flags and releases in code, we define constants for their names.
Each feature flag name must be added as a `const val` in `FeatureFlagNames.kt` and the constant should be added to the `featureFlagNames` list in the same file.
Similar, each release name must be added as a `const val` in `FeatureFlagGroupNames.kt` and the constant should be added to the `featureFlagGroupNames` list in the same file.

Config related to flipping strategies can be added under feature-flags or releases, nested under `strategy-config`.
Adding a `release-date` here will attach a release date flipping strategy to the flag or release.
Adding an `enabled-by-strategy` boolean config here will attach a boolean flipping strategy to the flag or release.

Example config:
```yaml
features:
    feature-flags:
        - name: "example-feature-flag-one"
          enabled: true
          expiry-date: "2030-01-12"
          strategy-config:
              release-date: "2025-01-12"
              enabled-by-strategy: true
        - name: "example-feature-flag-two"
          enabled: false
          expiry-date: "2030-01-07"
          release: "release-1-0"
    releases:
        - name: "release-1-0"
          enabled: true
          strategy-config:
              release-date: "2025-01-12"
```
Note in the example above, `example-feature-flag-two` would be enabled after `2025-01-12` because on the config on the `release-1-0` release.

### Implementation notes
Spring will load the configuration from application.yml into the `featureFlags` and `releases` properties on the `FeatureFlagConfig` due to the `@ConfigurationProperties(prefix = "features")` annotation on that class.

Spring will automatically add any `FlippingStrategyFactory` beans (such as `ReleaseDateFlipStrategyFactory`) it finds to `flippingStrategyFactories` in `FeatureFlipStrategyInitialiser`.



## Feature flagged services

You can define a service which calls different versions of a function depending on the value of a feature flag.

* Define an interface (see `ExampleFeatureFlaggedService.kt`)
* Define two implementations of the interface, annotated with `@PrsdbWebService("bean-name")`.
    * Add the `@Primary` annotation to the implementation that should be used be default (see `ExampleFeatureFlagServiceImplFlagOff`).
* Annotate members in your interface with `@PrsdbFlip(name = "...", alterBean = "...")` where the `alterBean` value matches the name you gave to your second implementation (see `ExampleFeatureFlagServiceImplFlagOn.kt`)`.

To use your feature flagged service, pass in the interface (see `ExampleFeatureFlagTestController`) - it will automatically call the correct implementation based on the feature flag value.

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


## Feature flag release demo
The enabled/disabled value of individual flags is effectively overridden by the release setting if the flag is in a release.
`EXAMPLE_FEATURE_FLAG_TWO` and `EXAMPLE_FEATURE_FLAG_THREE` have been added to the `RELEASE_1_0` release.

The release behaviour is demonstrated by a set of endpoints in `ExampleFeatureFlagTestController` (which expose the value set by developers in config to the user)
* `/feature-flagged-endpoint-test/feature-release/example-feature-flag-two`
  * Available when the `EXAMPLE_FEATURE_FLAG_TWO` feature is enabled
* `/inverse-feature-flagged-endpoint-test/feature-release/example-feature-flag-two`
  * Available when the `EXAMPLE_FEATURE_FLAG_TWO` feature is disabled
* `/feature-flagged-endpoint-test/feature-release/example-feature-flag-three`
  * Available when the `EXAMPLE_FEATURE_FLAG_THREE` feature is enabled
* `/inverse-feature-flagged-endpoint-test/feature-release/example-feature-flag-three`
  * Available when the `EXAMPLE_FEATURE_FLAG_THREE` feature is disabled

For a useful demo, check that in `featureFlags`
* `EXAMPLE_FEATURE_FLAG_TWO` is set to enabled = true
* `EXAMPLE_FEATURE_FLAG_THREE` is set to enabled = false

Then toggle the `RELEASE_1_0` release enabled setting to see the endpoints become available or unavailable as appropriate.

## Flipping strategies
The strategy on individual flags is overridden by the release strategy if the flag is in a release with a strategy.

### Demo
The release date flipping strategy is demonstrated by the following endpoints in `ExampleFeatureFlagTestController`
* `/feature-flagged-endpoint-test/feature-release/example-feature-flag-four`
  * Available when the `EXAMPLE_FEATURE_FLAG_FOUR` release date has passed (so the feature is enabled)
* `/inverse-feature-flagged-endpoint-test/feature-release/example-feature-flag-four`
  * Available when the `EXAMPLE_FEATURE_FLAG_FOUR` release date is in the future (so the feature is disabled)

For a useful demo, change the `release-date` on `"`release-with-strategy` release between a date in the past and a date in the future, then check the endpoints become available or unavailable as appropriate.

### Adding a new stratey type
To add a new kind of flipping strategy to the codebase:
* Add a fields for the strategy config to `FeatureFlagStrategyConfig` (see `releaseDate` and `enabledByStrategy` for examples)
* If it is not an in-built FF4J strategy, create a new class implementing `AbstractFlippingStrategy` (see `BooleanFlipStrategy.kt` for an example)
* Create a new `FlippingStrategyFactory` implementation (see `ReleaseDateFlipStrategyFactory.kt` for an example)
    * Override `getStrategyOrNull` to define how to create your strategy from the config
    * Annotate this with `@PrsdbWebComponent` so that it is picked up by Spring and injected into `flippingStrategyFactories` in `FeatureFlipStrategyInitialiser`

It is currently set up to add a custom `CombinedFlipStrategy` to the features which `AND`s together all strategies defined on a feature or release.


## Tests
Tests should inherit from FeatureFlagTest. This uses FeatureFlagConfig from test's version of application.yml to get flag values, but they also can be enabled or disabled in particular tests as required.

See the following for example tests:
* ExampleFeatureFlagServiceTest.kt
* ExampleFeatureFlaggedEndpointAvailabilityTest.kt

### Related tests
We can add controller tests in the usual way - the endpoints are called whether the feature is enabled or not because @WebMvcTest doesn't check the WebMvcRegistrations
* ExampleFeatureFlagTestControllerTests.kt
