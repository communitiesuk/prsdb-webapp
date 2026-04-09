---
applyTo: "**/featureFlags/**,**/annotations/**FeatureFlag**,**/annotations/**Flip**"
---

# Feature Flags Instructions

## Documentation Reference
- Full guide: [docs/FeatureFlagsReadMe.md](../../docs/FeatureFlagsReadMe.md)

## Configuration

### Define Flags in application.yml
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
