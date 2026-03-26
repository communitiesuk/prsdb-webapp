---
applyTo: "**/clients/**"
---

# API Clients Instructions

## Overview

External API clients live in `clients/` and abstract HTTP communication. Each client has a corresponding configuration class in `config/`.

## Client Patterns

### RestClient (preferred for new clients)

`EpcRegisterClient` uses Spring's `RestClient` with OAuth2 Bearer token injection:

```kotlin
// Config class creates the RestClient bean
RestClient.builder()
    .requestInterceptor { request, body, execution ->
        val token = authorizedClientManager.authorize(authRequest)
        request.headers.setBearerAuth(token.accessToken.tokenValue)
        execution.execute(request, body)
    }
    .baseUrl(baseUrl)
    .build()
```

Error handling uses `.onStatus()` callbacks:
```kotlin
.onStatus({ it == HttpStatus.NOT_FOUND }) { _, _ -> }  // Silent ignore
.onStatus({ it == HttpStatus.BAD_REQUEST }) { _, response ->
    throw PrsdbWebException("Bad request: ...")
}
```

### Java HttpClient (for non-Spring HTTP needs)

`OsDownloadsClient` uses `java.net.http.HttpClient` for direct HTTP calls with manual response handling and status code switching.

## Configuration

Client beans are created in dedicated config classes (e.g. `EpcRegisterConfig.kt`, `OsDownloadsConfig.kt`). Configuration values come from `application.yml`.

## Error Handling

- Clients throw custom exceptions (e.g. `PrsdbWebException`, `RateLimitExceededException`)
- Service layer converts client exceptions to appropriate responses
- Use `onStatus()` callbacks for RestClient error mapping

## Local Development Stubs

For local development, stub implementations replace real API calls:
- Stub services go in `local/services/`, annotated with `@Profile("local")` and `@Primary`
- Stub API controllers go in `local/api/controllers/`, annotated with `@Profile("local")`
- Configure `application-local.yml` to point API URLs to `http://localhost:8080/...`

When adding a new external API integration:
1. Create the client class in `clients/`
2. Create a config class with the HTTP client bean
3. Create a local stub service/controller with `@Profile("local")`
4. Add local URL overrides to `application-local.yml`
