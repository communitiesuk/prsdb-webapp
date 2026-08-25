# Feature Flags

We are implementing feature flags using [FF4j](https://ff4j.github.io/).

We are setting the flags statically - their values will be set in the codebase.

We are using an Aspect Oriented Programming (AOP) approach, using annotations to switch behavior based on the value of a feature flag.

Flags should have expiry dates to prevent unintentionally leaving feature flags in our code for a long time.
We are not stopping the flags from working when they expire, but we do have a test that checks whether each feature has a flag and whether
it is in date.

Feature flags can (optionally) be assigned to a release (each flag can only be in one release. This is using FF4J's Feature Group).
The "enabled" boolean on the release overrides the individual flag settings, allowing all features in the release to be switched on or off
by changing one setting.

We can also add flipping strategies to feature flags and releases if required. If a strategy config is added to a release, this will
override any strategy config on individual flags in that release.
Currently, we are set up to add ReleaseDateFlipStrategy (feature disabled until the release date), and a boolean flipping strategy (mainly
added to check that strategies could be combined)

## Feature flag name

Each feature flag should relate to a single ticket on Jira. This will either be the task/story ticket that relates to the feature flag, or the epic if this feature flag relates to a series of tickets.

We should include the ticket number in the feature flag name.
When we turn on the feature flag, we will check the ticket to be able to verify that all work related to the feature flag is fine to be released.

## Feature flag configuration

Default feature flags and releases are defined in `application.yml` under `features`.

These can be overridden in `application-<environment-name>.yml` files as required.

Note each `release` must be added to at least one `feature-flag` as empty releases are not allowed.

In order to refer to feature flags and releases in code, we define constants for their names.
Each feature flag name must be added as a `const val` in `FeatureFlagNames.kt` and the constant should be added to the `featureFlagNames`
list in the same file.
Similar, each release name must be added as a `const val` in `FeatureFlagGroupNames.kt` and the constant should be added to the
`featureFlagGroupNames` list in the same file.

Config related to flipping strategies can be added under feature-flags or releases, nested under `strategy-config`.
Adding a `release-date` here will attach a release date flipping strategy to the flag or release.
Adding an `enabled-by-strategy` boolean config here will attach a boolean flipping strategy to the flag or release.

Example config:

```yaml
features:
    feature-flags:
        -   name: "my-feature-flag"
            enabled: true
            expiry-date: "2030-01-12"
            strategy-config:
                release-date: "2025-01-12"
                enabled-by-strategy: true
        -   name: "another-feature-flag"
            enabled: false
            expiry-date: "2030-01-07"
            release: "my-release"
    releases:
        -   name: "my-release"
            enabled: true
            strategy-config:
                release-date: "2025-01-12"
```

Note in the example above, `another-feature-flag` would be enabled after `2025-01-12` because of the config on the `my-release`
release.

### Implementation notes

Spring will load the configuration from application.yml into the `featureFlags` and `releases` properties on the `FeatureFlagConfig` due to
the `@ConfigurationProperties(prefix = "features")` annotation on that class.

Spring will automatically add any `FlippingStrategyFactory` beans (such as `ReleaseDateFlipStrategyFactory`) it finds to
`flippingStrategyFactories` in `FeatureFlipStrategyInitialiser`.

## Feature flagged services

You can define a service which calls different versions of a function depending on the value of a feature flag.

* Define an interface
* Define two implementations of the interface, annotated with `@PrsdbWebService("bean-name")`.
    * Add the `@Primary` annotation to the implementation that should be used by default.
* Annotate members in your interface with `@PrsdbFlip(name = "...", alterBean = "...")` where the `alterBean` value matches the name you
  gave to your second implementation.
* You are also able to annotate the interface itself with `@PrsdbFlip` if you want to switch the whole service on and off based on a feature flag, rather than individual functions.

To use your feature flagged service, pass in the interface - it will automatically call the correct
implementation based on the feature flag value.

## Feature flagged endpoints

To make an endpoint available only when a feature is enabled, annotate it with `@AvailableWhenFeatureEnabled("flag-name")`

To make an endpoint available only when a feature is disabled, annotate it with `@AvailableWhenFeatureDisabled("flag-name")`


Currently, we enforce that only one of these annotations can be used on a given endpoint (with the `FeatureFlagAnnotationValidator`).

### Implementation notes

* Added two annotations that can be applied to endpoints: `AvailableWhenFeatureEnabled` and `AvailableWhenFeatureDisabled`.
    * This allows us to switch _off_ a particular endpoint (such as a placeholder) when a feature is enabled, as well as switching endpoints
      on.
    * Only one can be applied to a given endpoint (enforced by `FeatureFlagAnnotationValidator`)`.
    * If we decide to allow both annotations on the same endpoint in future, we should update `FeatureFlagHandlerMapping` to disable the
      endpoint if required by either flag.
* Added a request condition to be used with each annotation.
    * Currently only implemented `getMatchingCondition`
    * There are combine and compareTo methods which may be useful for combining or prioritizing multiple conditions if we need to do that in
      future.
* `FeatureFlagConditionMapping` - this checks every endpoint in the codebase, and applies the relevant request condition if one of the
  feature flag annotations is present.

## Feature flag releases

The enabled/disabled value of individual flags is effectively overridden by the release setting if the flag is in a release.

## Overriding flags during development

In non-production environments you can override any combination of feature flags and releases for your own browser
session, without affecting anyone else.

Go to `/system-operator/feature-flags`. The page is deliberately not linked from anywhere, so that with no overrides set
the service looks exactly as it does in production. It requires no sign-in and no role, because overrides are often
needed while testing the registration journeys, before a user has an account.

Each flag and release has a Default / On / Off control. Overrides are stored in the HTTP session and are cleared when
the session ends, or immediately using the "Reset all overrides" button. While any override is set, a banner appears on
every page linking back to this one.

Flags are resolved in this order:

1. an override on the flag's release, if one is set
2. an override on the flag itself, if one is set
3. the configured value, including any flipping strategy

Release overrides win over flag overrides so that you cannot produce a combination that would be unreachable in a real
deployment. A flag override still applies when the flag's release is disabled by configuration but carries no override
of its own, which is how you switch on a single flag inside an unreleased release. Because an override short-circuits
the check, it also supersedes any flipping strategy, so a flag can be forced on before its release date.

### Configuration

Overrides are controlled by `features.overrides-enabled`, which is set to `true` in `application-local.yml`,
`application-integration.yml`, `application-test.yml` and `application-nft.yml`. It is deliberately absent from
`application.yml` and defaults to `false`, so in production the controller bean is never registered, the route is never
permitted by the security config, and `FeatureFlagOverrideService` returns no overrides even if a session attribute were
somehow present.

Overrides are not persisted to the database. `FeatureFlagOverrideService` is the only component that knows where they
are stored, so adding persistence later means changing that class alone. It would also need gating on the user being
signed in and holding a role, so that there is a `prsdb_user` record to attach the overrides to.

## Flipping strategies

The strategy on individual flags is overridden by the release strategy if the flag is in a release with a strategy.
Flipping strategies are optional but can be useful e.g. for switching on a release on a particular date without having to manually deploy on that day.


### Adding a new strategy type

To add a new kind of flipping strategy to the codebase:

* Add a fields for the strategy config to `FeatureFlagStrategyConfig` (see `releaseDate` and `enabledByStrategy` for examples)
* If it is not an in-built FF4J strategy, create a new class implementing `AbstractFlippingStrategy` (see `BooleanFlipStrategy.kt` for an
  example)
* Create a new `FlippingStrategyFactory` implementation (see `ReleaseDateFlipStrategyFactory.kt` for an example)
    * Override `getStrategyOrNull` to define how to create your strategy from the config
    * Annotate this with `@PrsdbWebComponent` so that it is picked up by Spring and injected into `flippingStrategyFactories` in
      `FeatureFlipStrategyInitialiser`

It is currently set up to add a custom `CombinedFlipStrategy` to the features which `AND`s together all strategies defined on a feature or
release.

## Tests

Tests should inherit from FeatureFlagTest. This uses FeatureFlagConfig from test's version of application.yml to get flag values, but they
also can be enabled or disabled in particular tests as required.

**Important:** Feature flags are automatically reset to their default configuration (from `application.yml`) after each test completes. This
prevents flag changes from leaking between tests. You can freely modify feature flags within a test without needing to manually clean up
afterwards.

### Controller tests

We can add controller tests in the usual way - the endpoints are called whether the feature is enabled or not because @WebMvcTest doesn't
check the WebMvcRegistrations


### Integration tests

By default, integration tests use the feature flag config from `application.yml` (or `application-<env>.yml` if it exists).
But we can modify the configuration and run tests with different flag settings as required.

* Simple updates (enable / disable an individual flag or release) can be done by calling the relevant `FeatureFlagManager` method before
  running the test
* For more complex updates, there is a `FeatureFlagConfigUpdater` test helper which will update flipping strategies or re-initialize all
  flags and releases as required.
* If a feature flag is inside an unreleased feature, enabling it does nothing. This is since the release takes priority. Use
  `FeatureFlagConfigUpdater#enableUnreleasedFeature` to bypass this.

**Note:** Like unit tests, feature flags are automatically reset after each integration test completes, so you don't need to manually
restore the original configuration.

## Development workflow for feature flags

Our current workflow for managing feature flags is:

1. When developing the feature, add a new feature flag for the feature or add it to an existing feature flag.
2. When testing, the feature flag can be toggled on or off per environment.
3. Once the feature is ready for release, the feature flag can be added to a release and managed by the release config.
