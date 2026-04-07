---
applyTo: "**/config/**,**/security/**,**/filters/**,**/interceptors/**"
---

# Config Instructions

## Package Structure

```
config/
├── security/          # OAuth2 security filter chains
├── filters/           # Servlet filters (CSP, CSRF, multipart, etc.)
├── interceptors/      # Spring MVC interceptors
├── resolvers/         # OAuth2 request resolvers
└── ...                # Feature flags, factories, managers, other config
```

## Security Configuration

The application uses a **3-tier OAuth2 security setup** with GOV.UK One Login, ordered by specificity:

| Config | Order | Matches | Purpose |
|--------|-------|---------|---------|
| `LandlordSecurityConfig` (ID verification chain) | 1 | `/id-verification/**` | Identity verification with elevated trust level |
| `LandlordSecurityConfig` (main chain) | 2 | `/landlord/**` | Landlord routes with session management |
| `LocalCouncilSecurityConfig` | 3 | `/local-council/**` | Local council routes |
| `DefaultSecurityConfig` | LOWEST | Everything else | Public routes, CSP, permissions policy |

**Key features:**
- `@EnableMethodSecurity` enables `@PreAuthorize` annotations on controllers
- `UserServiceFactory.create(roleIssuer)` provides pluggable role mapping per user type
- Public endpoints (assets, health, error, cookies) are explicitly permitted

## Filters

| Filter | Purpose | Positioned |
|--------|---------|-----------|
| `CSPNonceFilter` | Generates per-request nonce for Content Security Policy | Before `HeaderWriterFilter` |
| `OauthTokenSecondaryValidatingFilter` | Validates OAuth2 token acceptability | After `SecurityContextHolderFilter` |
| `MultipartFormDataFilter` | Extracts CSRF token from multipart form data | Before `CsrfFilter` |
| `InvalidCoreIdentityFilter` | Catches identity verification failures | After OAuth validation |
| `TrailingSlashFilterConfiguration` | 308 redirect for trailing slash normalisation | Highest precedence |

## Interceptors

Interceptors implement `HandlerInterceptor` and are registered via `WebMvcConfigurer`:

```kotlin
@PrsdbWebConfiguration
class MyInterceptorConfig(private val myService: MyService) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(MyInterceptor(myService))
    }
}
```

Existing interceptors:
- `BackLinkInterceptor` — stores/retrieves back URL via query parameter
- `MaintenanceInterceptor` — active only with `@Profile("maintenance-mode")`
- `PasscodeInterceptor` — active only with `@Profile("require-passcode")`

## Custom Annotations for Config Classes

Use the project's custom annotations instead of plain Spring annotations:
- `@PrsdbWebConfiguration` — for web server configuration beans
- `@PrsdbTaskConfiguration` — for scheduled task configuration beans

## Feature Flag Config Validation

`FeatureFlagConfig` enforces strict consistency between YAML configuration and code constants at startup:
- All flags in `application.yml` must have a corresponding constant in `FeatureFlagNames.kt`
- All constants in `FeatureFlagNames.kt` must exist in `application.yml`
- Same enforcement applies to release names via `FeatureFlagReleaseNames.kt`
- Throws `IllegalStateException` on mismatch, preventing config drift

## Profile-Specific Configuration

| Profile | File | Purpose |
|---------|------|---------|
| (default) | `application.yml` | Base configuration |
| `local` | `application-local.yml` | Local dev (local DB, Redis, Flyway, no template caching) |
| `integration` | `application-integration.yml` | Integration environment with feature flags |
| `maintenance-mode` | (programmatic) | Activates maintenance interceptor |
| `require-passcode` | (programmatic) | Activates passcode interceptor |
