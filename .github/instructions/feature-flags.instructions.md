---
applyTo: "**/featureFlags/**,**/annotations/**FeatureFlag**,**/annotations/**Flip**"
---

# Feature Flags Instructions

## Documentation Reference
- Full guide: [docs/FeatureFlagsReadMe.md](../../docs/FeatureFlagsReadMe.md)

## Configuration

### Define Flags in ALL yaml files
A flag must be added to **every** feature-flags config file, or startup validation
(`FeatureFlagConfig`) / consistency tests will fail. Do not stop after the two base
`application.yml` files — the environment-specific files must be updated too.

| File | Typical `enabled` for a new dev flag |
|------|--------------------------------------|
| `src/main/resources/application.yml` (prod/default) | `false` |
| `src/test/resources/application.yml` | `false` |
| `src/main/resources/application-local.yml` | `true` |
| `src/main/resources/application-integration.yml` | `true` |
| `src/test/resources/application-integration.yml` | `true` |
| `src/main/resources/application-test.yml` | `true` |
| `src/main/resources/application-nft.yml` | `false` |

Flags are typically on in local/integration/test and off in nft and prod, but confirm
the intended per-environment state with the user rather than assuming.

Entry format (indentation differs slightly between files — match the surrounding entries):
```yaml
features:
    feature-flags:
        -   name: "my-feature-flag"
            enabled: false
            expiry-date: "2026-06-01"
            release: "release-name"  # optional
    releases:
        -   name: "release-name"
            enabled: true
```

### Naming
Flag names follow the epic, e.g. `pdjb-1040-correspondence-address` (lowercase
kebab-case, correct spelling). The Kotlin constant is the SCREAMING_SNAKE_CASE
equivalent, e.g. `CORRESPONDENCE_ADDRESS`.

### Reference implementation
See PR communitiesuk/prsdb-webapp#1761 (PDJB-1397) for a complete "create a feature flag"
change touching all the files above.

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
`FeatureFlagConfig` enforces strict consistency at startup:
- All flags in `application.yml` must have a constant in `FeatureFlagNames.kt` (and vice versa)
- All releases in `application.yml` must have a constant in `FeatureFlagReleaseNames.kt` (and vice versa)
- Startup fails with `IllegalStateException` if any mismatch is found

## Usage Patterns

### Feature-Flagged Endpoints
```kotlin
@GetMapping("/new-feature")
@AvailableWhenFeatureEnabled(MY_FEATURE_FLAG)
fun newFeature(): ModelAndView { }

@GetMapping("/old-feature")
@AvailableWhenFeatureDisabled(MY_FEATURE_FLAG)
fun oldFeature(): ModelAndView { }
```

### Feature-Flagged Services
```kotlin
// Interface with flip annotation
interface MyService {
    @PrsdbFlip(name = MY_FEATURE_FLAG, alterBean = "newImpl")
    fun doSomething()
}

// Default implementation (flag off)
@PrsdbWebService
@Primary
class MyServiceImpl : MyService { }

// Alternative implementation (flag on)
@PrsdbWebService("newImpl")
class MyServiceNewImpl : MyService { }
```

## Testing

### Unit Tests
```kotlin
class MyFeatureFlagTests : FeatureFlagTest() {
    
    @Test
    fun `behaves differently when flag enabled`() {
        enableFeature(MY_FEATURE_FLAG)
        // test behavior
    }
}
```

### Integration Tests
```kotlin
@Test
fun `new page available when flag enabled`() {
    featureFlagManager.enable(MY_FEATURE_FLAG)
    // Flags auto-reset after test
}
```

## Key Rules
- Flags must have expiry dates (enforced by tests)
- Only one of `@AvailableWhenFeatureEnabled` / `@AvailableWhenFeatureDisabled` per endpoint
- Release settings override individual flag settings

## Development Overrides

In non-production environments, `/system-operator/feature-flags` lets a developer override flags and releases for their
own session. It is gated by `features.overrides-enabled`, which defaults to false and is absent from `application.yml`,
so it can never be active in production. See [docs/FeatureFlagsReadMe.md](../../docs/FeatureFlagsReadMe.md).
