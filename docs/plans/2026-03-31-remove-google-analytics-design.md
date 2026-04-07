# Remove Google Analytics — Design

**Ticket:** PDJB-727
**Date:** 2026-03-31

## Problem

Google Analytics (GA4) is no longer being used. It must be removed from the
build before Private Beta launch. The application also uses Plausible
analytics, which will remain.

## Approach

Complete removal of GA4/GTM code and the cookie consent banner in a single
pass. The cookie consent mechanism exists solely to support GA cookies;
Plausible is cookieless and does not require consent.

## Changes by Layer

### HTML Template (layout.html)

- Remove 4 GA-related `<script>` blocks: consent defaults, GTM async loader,
  GA config, and environment variable injection.
- Remove cookie banner fragment include.
- Remove Thymeleaf variable declarations for GA model attributes.

### JavaScript

- Delete `cookieConsentHandler.js` — all logic is GA/consent-banner specific.
- Delete `googleAnalyticsSetUp.js` — unused module duplicating layout.html code.
- Remove the import and invocation from `index.js`.

### Backend Kotlin

- `GlobalModelAttributes.kt`: Remove GA measurement ID, cookie domain, and
  GTM URL model attributes.
- `DefaultSecurityConfig.kt`: Remove GA/GTM URLs from Content Security Policy
  directives.
- `AnalyticsUrlConstants.kt`: Remove GA and GTM URL constants (keep Plausible).

### Configuration

- `application.yml`, `application-local.yml`, `src/test/resources/application.yml`:
  Remove `google-analytics` section.

### Content / Messages

- `cookies.yml`: Remove analytics cookies section. Keep essential cookies.
- `cookies.html`: Remove analytics section and consent form. Add TODO for
  design review.
- `cookieBanner.html`: Delete (no longer referenced).

### Tests

- Delete `cookieConsentHandler.test.js`.
- Remove `CookieBannerTests.kt` and its page object.
- Update `CookiesPageTests.kt` to remove consent form tests.
- Clean up page objects referencing removed elements.

## Out of Scope

- Privacy notice (`localCouncilPrivacyNotice.yml`) — legal team to review.
- ADR 0032 (Web Analytics) — left as historical record.
- Plausible analytics — untouched.
- Deployment environment variables — removal handled by ops separately.
