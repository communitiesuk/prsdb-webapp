---
applyTo: "**/config/featureFlags/**,**/FeatureFlag*.kt,**/FeatureRelease*.kt,**/FeatureFlipStrategy*.kt,**/config/conditions/*FeatureFlag*.kt,**/config/flipStrategies/**,**/config/factories/*Flip*StrategyFactory.kt,**/annotations/webAnnotations/AvailableWhenFeature*.kt,**/annotations/webAnnotations/PrsdbFlip.kt,**/config/security/DefaultSecurityConfig.kt,**/controllers/controllerAdvice/GlobalModelAttributes.kt,**/PrsdbWebMvcRegistration.kt,**/resources/application*.yml,**/templates/**/featureFlagOverride*.html,**/messages/featureFlagOverride*.yml"
---

# Feature Flags Instructions

## Documentation Reference
- Background: [docs/FeatureFlagsReadMe.md](../../docs/FeatureFlagsReadMe.md) contains legacy class names and an outdated
  production-override guarantee. Use the current source and this reference for implementation details.
- `config/featureFlags/` is a **test** package. Runtime code is spread across `config/`, annotations, constants, models,
  controllers and services. Current names include `FeatureFlagReleaseNames`, `FeatureFlagConditionMapping`,
  `FeatureFlipStrategyConfigModel` and FF4j's `AbstractFlipStrategy`.

## Configuration

### Define Flags in application.yml
Choose an expiry date that is still in the future when adding the flag; the date below is illustrative, not a permanent default.

```yaml
features:
    feature-flags:
        -   name: "my-feature-flag"
            enabled: false
            expiry-date: "2027-12-31"  # Replace with an appropriate future cleanup date
            release: "my-release"  # optional; must match the registered release
    releases:
        -   name: "my-release"
            enabled: true
```

Update profile-specific lists as well: `local`, `integration`, `test`, `nft`, and any relevant
`src/test/resources/application*.yml` overrides. Spring binds the active list, rather than merging individual flags by name.
Each defined release needs **at least one assigned flag** so FF4j can create its group. An empty release list is valid
when no releases are registered.

### Register Flag Names
Add to `FeatureFlagNames.kt`:
```kotlin
const val MY_FEATURE_FLAG = "my-feature-flag"

val featureFlagNames = listOf(
    // ... existing flags
    MY_FEATURE_FLAG
)
```

### Register Release Names
Add to `FeatureFlagReleaseNames.kt`:
```kotlin
const val MY_RELEASE = "my-release"

val featureFlagReleaseNames = listOf(
    // ... existing releases
    MY_RELEASE
)
```

### Config Validation
[FeatureFlagConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/FeatureFlagConfig.kt) compares the active
bound flag/release lists against `featureFlagNames` / `featureFlagReleaseNames` in both directions:
- Add each name as a constant **and include it in its registration list**. Constants alone are not discovered by reflection.
- Every bound name must be registered, and every registered name must appear in the active bound configuration.
- A mismatch throws `IllegalStateException` at startup.
- Keep [FeatureFlagConsistencyTests](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/config/featureFlags/FeatureFlagConsistencyTests.kt)
  coverage in step with profile lists; it checks default, local, integration, test and nft flag-name consistency.

Strategies bind through `FeatureFlipStrategyConfigModel` (`releaseDate`, `enabledByStrategy`). `FeatureFlipStrategyInitialiser`
uses `FlippingStrategyFactory` implementations and combines their strategies with `CombinedFlipStrategy`.

## Usage Patterns

### Feature-Flagged Endpoints
The single root `PrsdbWebMvcRegistration` installs `FeatureFlagConditionMapping`, which creates
`FeatureFlaggedRequestCondition` / `InverseFeatureFlaggedRequestCondition` for annotated handlers.

```kotlin
@GetMapping("/new-feature")
@AvailableWhenFeatureEnabled(MY_FEATURE_FLAG)
fun newFeature(): ModelAndView = ModelAndView("newFeature")

@GetMapping("/old-feature")
@AvailableWhenFeatureDisabled(MY_FEATURE_FLAG)
fun oldFeature(): ModelAndView = ModelAndView("oldFeature")
```

### Feature-Flagged Services
Use `featureFlagManager.checkFeature(MY_FEATURE_FLAG)` for normal runtime decisions, including session overrides.
Use `checkConfiguredFeature(...)` only when the configured baseline (including release settings and strategies, but
excluding session overrides) is required, as on the override page.

`@PrsdbFlip` remains supported for FF4j service switching, although current main code has no call sites. An illustrative pattern:

```kotlin
// Interface with flip annotation
interface MyService {
    @PrsdbFlip(name = MY_FEATURE_FLAG, alterBean = "newImpl")
    fun doSomething()
}

// Default implementation (flag off)
@PrsdbWebService
@Primary
class MyServiceImpl : MyService {
    override fun doSomething() { /* existing behaviour */ }
}

// Alternative implementation (flag on)
@PrsdbWebService("newImpl")
class MyServiceNewImpl : MyService {
    override fun doSomething() { /* alternative behaviour */ }
}
```

## Testing

### Spring Context Tests
[FeatureFlagTest](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/config/featureFlags/FeatureFlagTest.kt) is a
`@SpringBootTest` base, not an isolated unit-test fixture. It injects the manager/config and resets configured flags/releases
in its `@AfterEach`.

```kotlin
class MyFeatureFlagTests : FeatureFlagTest() {
    @Test
    fun `behaves differently when flag enabled`() {
        featureFlagManager.enableFeature(MY_FEATURE_FLAG)
        // test behavior
    }
}
```

### Integration Tests
```kotlin
@Test
fun `new page available when flag enabled`() {
    featureFlagManager.enableFeature(MY_FEATURE_FLAG)
    // exercise the endpoint using the test fixture
}
```

`featureFlagManager.enable(...)` is valid inherited FF4j API; prefer `enableFeature(...)` / `disableFeature(...)`
for consistency. There is no unqualified `enableFeature(...)` helper on `FeatureFlagTest`. Reset is supplied by specific
test bases, not by every test automatically: arrange cleanup when your fixture does not provide it, including session overrides.

## Key Rules
- Flags need future expiry dates (checked by `FeatureFlagExpiryDateTests`).
- Use only one of `@AvailableWhenFeatureEnabled` / `@AvailableWhenFeatureDisabled` per handler; `FeatureFlagAnnotationValidator`
  rejects conflicts.
- [FeatureFlagManager](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/managers/FeatureFlagManager.kt) initialises
  flags first, then releases. A release sets its flags' enabled state; it replaces their strategies **only when the release's
  `strategyConfig` is non-null**.
- Runtime precedence is **release session override > flag session override > configured manager check**.

## Development Overrides

`/system-operator/feature-flags` is a development-only tool by policy, **disabled by default**:
`features.overrides-enabled: false` is present in [base application.yml](../../src/main/resources/application.yml).

- `FeatureFlagOverridesEnabled` is a Spring `Condition`. It and the route guard in `DefaultSecurityConfig` check the
  property, not a production/non-production profile. `FeatureFlagOverrideService` also checks the property and needs
  an active request context. These guards do **not** make production activation impossible; keep deployed production
  configuration disabled. This is not evidence that production has overrides enabled.
- When enabled, [FeatureFlagOverrideController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/FeatureFlagOverrideController.kt)
  intentionally has no role requirement and the route is `permitAll()` (normal POST CSRF protection still applies).
- [FeatureFlagOverrideService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/FeatureFlagOverrideService.kt)
  owns overrides in the current session. The page does not mutate global manager configuration.
- Keep the override request/view models, controller/service tests, `featureFlagOverrides.html`, override banner and
  `featureFlagOverride*.yml` messages consistent with these semantics.
