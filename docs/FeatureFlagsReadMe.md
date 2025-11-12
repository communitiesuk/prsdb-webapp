# Feature Flags

We are implementing feature flags using [FF4j](https://ff4j.github.io/).

We are setting the flags statically - their values will be set in the codebase.

We are using an Aspect Oriented Programming (AOP) approach, using annotations to switch behavior based on the value of a feature flag.

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
