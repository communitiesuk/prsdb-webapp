---
applyTo: "**/config/**,**/security/**,**/filters/**,**/interceptors/**,**/PrsdbWebappApplication.kt,**/PrsdbWebMvcRegistration.kt,**/resources/application*.yml,**/annotations/webAnnotations/PrsdbWeb*.kt,**/annotations/webAnnotations/Prsdb*Controller*.kt,**/annotations/webAnnotations/JourneyFrameworkComponent.kt,**/annotations/webAnnotations/WebServerOnly.kt,**/annotations/taskAnnotations/**"
---

# Config Instructions

## Package Structure

```
config/
├── security/          # Ordered security filter chains
├── filters/           # Servlet filters (CSP, CSRF, multipart, etc.)
├── interceptors/      # Spring MVC interceptors
├── resolvers/         # OAuth2 request resolvers
└── ...                # Feature flags, factories, managers, other config
```

Also covers the root `PrsdbWebappApplication` bootstrap, `PrsdbWebMvcRegistration`, `resources/application*.yml`
and mode-specific stereotypes/conditions.

## Security Configuration

The application defines **five `SecurityFilterChain` beans**, ordered by specificity:

| Config | Order | Matches | Purpose |
|--------|-------|---------|---------|
| [LandlordSecurityConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/security/LandlordSecurityConfig.kt) (ID verification) | 1 | Registration and joint-invitation `IdentityVerifyingStep` routes, plus `/id-verification/**` | One Login identity verification with elevated trust level |
| `LandlordSecurityConfig` (main chain) | 2 | `/landlord/**` | One Login landlord routes, explicit public entry points and session management |
| [LocalCouncilSecurityConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/security/LocalCouncilSecurityConfig.kt) | 3 | `/local-council/**` | Internal Access (`internal-access`) OAuth2 login, not One Login |
| [LettingAgentSecurityConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/security/LettingAgentSecurityConfig.kt) | 4 | `/letting-agent/**` | Session/invitation access; selected routes are anonymous-only; no `oauth2Login` |
| [DefaultSecurityConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/security/DefaultSecurityConfig.kt) | `Ordered.LOWEST_PRECEDENCE` | Everything not matched above | Explicit public routes; authentication via One Login for other requests |

**Key features:**
- Only the **first matching chain** handles a request. The default chain is not a fallback for failed authorization,
  and its filters/headers are not inherited by earlier chains.
- `@EnableMethodSecurity` enables `@PreAuthorize` annotations on controllers
- `UserServiceFactory.create(roleIssuer)` provides pluggable role mapping per user type
- Public endpoints (assets, health, error, cookies) and journey entry points are explicitly permitted in their owning chain

## Filters

| Filter | Purpose | Placement / scope |
|--------|---------|-------------------|
| `CSPNonceFilter` | Generates per-request nonce for Content Security Policy | Before `HeaderWriterFilter` in landlord-main, council, letting-agent and default chains |
| `OauthTokenSecondaryValidatingFilter` | Validates OAuth2 token acceptability | After `SecurityContextHolderFilter` in the ID-verification chain |
| `MultipartFormDataFilter` | Reads multipart CSRF data and exposes the streaming upload iterator | Before `CsrfFilter` in the landlord-main chain |
| `InvalidCoreIdentityFilter` | Catches identity verification failures | After `OauthTokenSecondaryValidatingFilter` in the ID-verification chain |
| `TrailingSlashFilterConfiguration` | 308 redirect for trailing slash normalisation | Servlet filter registration at highest precedence, outside the chain-specific additions |

Preserve chain-specific registration when changing security. In particular, the ID-verification chain does not inherit
the default chain's CSP filter/header configuration. Ordinary Spring multipart handling is disabled in `application.yml`;
see [controller upload handling](controllers.instructions.md#multipart-uploads).

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
- `BackLinkInterceptor` — reads the query's back-URL key, retrieves the URL via its callback and propagates the key on
  redirects/forwards. `BackUrlStorageService`, not the interceptor, owns storage.
- `MaintenanceInterceptor` — active only with `@Profile("maintenance-mode")`
- `PasscodeInterceptor` — active only with `@Profile("require-passcode")`
- `PlausibleInterceptor` — adds current-URL/referrer model attributes for rendered views, skipping redirects/forwards

## MVC Registration

The root [PrsdbWebMvcRegistration](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/PrsdbWebMvcRegistration.kt)
installs `FeatureFlagConditionMapping`. Keep a **single `WebMvcRegistrations` bean**; do not add a second registration
that would bypass the custom feature-flag request conditions.

## Custom Annotations for Config Classes

Choose stereotypes by lifecycle, not by a blanket ban on plain Spring annotations:
- `@PrsdbWebConfiguration` — web-server-only configuration via `WebServerOnly`, which excludes the
  `web-server-deactivated` profile. Web controller/service/component stereotypes use the same mode guard;
  `@JourneyFrameworkComponent` also gives journey components prototype scope.
- `@PrsdbTaskConfiguration` / `@PrsdbTaskService` — task-mode configuration/services via `TaskOnly`, which requires
  `web-server-deactivated`, but does not require `scheduled-task` or a task-name profile.
- `WebServerOnly` and `TaskOnly` are Spring `Condition` implementations, not annotations to apply directly.
- Shared infrastructure may use plain `@Configuration`: `AuditingConfig`, `NotifyConfig` and `FeatureFlagConfig` are
  valid examples. Shared `FeatureFlagManager`, `FeatureFlipStrategyInitialiser` and strategy factories also use `@Component`.

See [scheduled task instructions](scheduled-tasks.instructions.md) for the extra activation rules on runner annotations.

## Feature Flag Config Validation

`FeatureFlagConfig` compares the **active bound** `featureFlags` and `releases` lists with the registered
`featureFlagNames` and `featureFlagReleaseNames` lists in both directions. Mismatches throw `IllegalStateException`
at startup. It does **not** reflect over every Kotlin constant: constants must also be added to the registration lists.
Profile-specific YAML lists must stay consistent too. See [feature flag instructions](feature-flags.instructions.md).

## Profile-Specific Configuration

| Profile | File | Purpose |
|---------|------|---------|
| (default) | `application.yml` | Base configuration |
| `local` | `application-local.yml` | Local dev (local DB, Redis, Flyway, no template caching) |
| `integration` | `application-integration.yml` | Integration environment with feature flags |
| `test` | `application-test.yml` | Test-environment configuration and flags |
| `nft` | `application-nft.yml` | Non-functional-test configuration and flags |
| `web-server-deactivated` | Profile document in `application.yml` | Sets `spring.main.web-application-type: none` |
| `maintenance-mode` | (programmatic) | Activates maintenance interceptor |
| `require-passcode` | (programmatic) | Activates passcode interceptor |

Account for `src/test/resources/application*.yml` overrides when reviewing test configuration.
