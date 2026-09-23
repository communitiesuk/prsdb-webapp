---
applyTo: "**/clients/**"
---

# API Clients Instructions

## Overview

External integrations in `clients/` adapt HTTP APIs or SDKs. Keep application-facing adapters distinct from the
transport beans supplied by configuration classes in `config/`.

## Client Patterns

### RestClient (preferred for new HTTP clients)

[EpcRegisterClient](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/clients/EpcRegisterClient.kt) and
[PlausibleClient](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/clients/PlausibleClient.kt) are scanned
`@PrsdbWebService` adapters that inject qualified `RestClient` transports:

```kotlin
@PrsdbWebService
class EpcRegisterClient(
    @Qualifier("epc-client") private val client: RestClient,
) {
    // API operations
}
```

[EpcRegisterConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/EpcRegisterConfig.kt) defines the
`epc-client` transport and OAuth2 registration/interceptor contract. `OAuth2AuthorizedClientManager.authorize(...)`
can return null: new code must handle failed authorization explicitly, not dereference it unconditionally or copy
the existing empty-token fallback.

[PlausibleApiConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/PlausibleApiConfig.kt) supplies
`plausible-stats-client`. The adapter posts a typed `PlausibleQuery` to `/api/v2/query` and reads a
`PlausibleQueryResponse`, using default RestClient status handling rather than custom `.onStatus()` mappings.

### Java HttpClient (for non-Spring HTTP needs)

[OsDownloadsClient](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/clients/OsDownloadsClient.kt) uses
`java.net.http.HttpClient`. Task-only
[OsDownloadsConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/OsDownloadsConfig.kt) constructs the adapter bean.

- API requests accept 200 or 307, map 429 to `RateLimitExceededException`, and throw Apache `org.apache.http.HttpException` for other statuses.
- File retrieval follows `Location` with a separate request that does not forward the API key. This download request
  accepts only 200; other statuses throw `HttpException`. Callers must close the returned `InputStream` (for example, with `use`).

### SDK Clients

[CloudWatchMetricsClient](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/clients/CloudWatchMetricsClient.kt) and
[CostExplorerMetricsClient](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/clients/CostExplorerMetricsClient.kt)
are interfaces with `Aws*` implementations under `@Profile("!local")` and `Stub*` implementations under
`@Profile("local")`, all in `clients/`. Inject the interfaces. These profiles are mutually exclusive, so selecting
real versus stub does not need `@Primary`.

Reuse configured SDK beans:
[CloudWatchConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/CloudWatchConfig.kt) supplies a primary
region-provider-based client and an `@Qualifier("cloudFrontCloudWatchClient")` client in `Region.US_EAST_1`;
[CostExplorerConfig](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/config/CostExplorerConfig.kt) also uses
`Region.US_EAST_1`. See the [metrics guide](../../docs/MetricsReadMe.md) for integration details.

## Configuration

Configuration owns transport beans, authentication, base URLs, regions and qualifiers, with values from `application.yml`
and environment/profile overrides. It does not necessarily construct the adapter: EPC and Plausible adapters are scanned.
For changes outside `clients/`, use the related [configuration](config.instructions.md),
[service](services.instructions.md) and [controller](controllers.instructions.md) instructions.

## Error Handling

- Status mapping is API-specific; do not apply a universal ignore-errors callback.
- EPC suppresses the default 404 status exception so its response body can be parsed as a no-match result.
- For EPC 400 responses, `INVALID_REQUEST` and `BAD_REQUEST` bodies are preserved for
  [EpcLookupService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/services/EpcLookupService.kt) to translate;
  other 400 bodies cause `PrsdbWebException`. Other statuses retain default RestClient handling.
- Preserve custom, library or SDK exception contracts as appropriate. Services decide domain/HTTP outcomes;
  not every client maps errors to `PrsdbWebException`.

## Local Development Stubs

Local behaviour is integration-specific, not always `local/services/` plus `@Primary`:
- CloudWatch and Cost Explorer use the profile-selected stubs in `clients/` described above.
- EPC still uses staging endpoints locally.
- Plausible uses `http://localhost:${server.port}/local/plausible`, not a hard-coded port. See
  [application-local.yml](../../src/main/resources/application-local.yml) for these URL overrides.
- Notify's [EmailNotificationStubService](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/local/services/EmailNotificationStubService.kt)
  lives in `local/services/` with `@Profile("local & !use-notify")` and `@Primary`; enabling `use-notify` opts into real sending.
  See the [Notify guide](../../docs/NotifyEmailsReadMe.md) for details.

For HTTP stubs, see `local/api/controllers/` and follow the existing profile/controller conventions.

When adding a new external API integration:
1. Add the adapter in `clients/`, using an SDK interface where that fits the integration.
2. Configure transport beans with the required lifecycle, authentication and qualifiers.
3. Choose and document the local contract: profile-selected implementation, local HTTP stub, or a deliberate external endpoint.
4. Add URL overrides to `application-local.yml` only where the local contract needs them.
