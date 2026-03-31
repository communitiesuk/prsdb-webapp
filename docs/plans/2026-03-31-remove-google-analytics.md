# Remove Google Analytics Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Remove all Google Analytics (GA4/GTM) code and the cookie consent banner from the application before Private Beta launch.

**Architecture:** Remove GA in layers: configuration → backend → template → JavaScript → content/messages → tests. The cookie consent banner and its handler are removed entirely since they exist solely to support GA cookies. Plausible analytics is untouched. The cookies page and controller remain (with a TODO for design review) since essential cookies still need documentation.

**Tech Stack:** Kotlin/Spring Boot, Thymeleaf templates, JavaScript (Rollup bundled), Node.js test runner, Playwright integration tests, Gradle build

---

### Task 1: Remove GA configuration from application YAML files

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-local.yml`
- Modify: `src/test/resources/application.yml`

**Step 1: Remove GA section from main application.yml**

In `src/main/resources/application.yml`, delete these lines:

```yaml
google-analytics:
  measurement-id: ${GOOGLE_ANALYTICS_MEASUREMENT_ID}
  cookie-domain: ${GOOGLE_ANALYTICS_COOKIE_DOMAIN}
```

**Step 2: Remove GA section from application-local.yml**

In `src/main/resources/application-local.yml`, delete these lines:

```yaml
google-analytics:
  measurement-id: G-NOTATAG123
  cookie-domain: localhost
```

**Step 3: Remove GA section from test application.yml**

In `src/test/resources/application.yml`, delete these lines:

```yaml
google-analytics:
  measurement-id: G-XXXXXXXXXX
  cookie-domain: example.gov.uk
```

---

### Task 2: Remove GA URL constants

**Files:**
- Modify: `src/main/kotlin/uk/gov/communities/prsdb/webapp/constants/AnalyticsUrlConstants.kt`

**Step 1: Remove GA and GTM constants, keep only Plausible**

Replace the entire file content with:

```kotlin
package uk.gov.communities.prsdb.webapp.constants

const val PLAUSIBLE_URL = "https://plausible.io"
```

---

### Task 3: Remove GA model attributes from GlobalModelAttributes

**Files:**
- Modify: `src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/controllerAdvice/GlobalModelAttributes.kt`

**Step 1: Remove GA field declarations**

Remove these fields:

```kotlin
@Value("\${google-analytics.measurement-id}")
private lateinit var gaMeasurementId: String

@Value("\${google-analytics.cookie-domain}")
private lateinit var gaCookieDomain: String
```

**Step 2: Remove GA model attributes from addGlobalModelAttributes**

Remove these three lines from the `addGlobalModelAttributes` method:

```kotlin
model.addAttribute("googleAnalyticsMeasurementId", gaMeasurementId)
model.addAttribute("googleAnalyticsCookieDomain", gaCookieDomain)
model.addAttribute("googleTagManagerUrl", "$GOOGLE_TAG_MANAGER_URL/gtag/js?id=")
```

Also remove the `// Cookie banner attributes` comment above the `cookiesUrl` line (since there's no longer a cookie banner), or update it to something appropriate.

**Step 3: Remove unused imports**

Remove the import for `GOOGLE_TAG_MANAGER_URL`:

```kotlin
import uk.gov.communities.prsdb.webapp.constants.GOOGLE_TAG_MANAGER_URL
```

Also remove the import for `COOKIES_ROUTE` and the `cookiesUrl` model attribute, as these were only used for the cookie banner. However — check: the cookies page link in the footer may still need a URL. If the footer uses a hardcoded path or a separate mechanism, remove it. If the footer relies on this attribute, keep it.

**Investigation needed:** Check `src/main/resources/templates/fragments/footer.html` to see if it uses the `cookiesUrl` model attribute or constructs the cookies link another way. The `cookiesUrl` attribute includes back-link override logic that may only be relevant to the cookie banner — the footer's cookies link likely uses a simpler path.

---

### Task 4: Remove GA from Content Security Policy

**Files:**
- Modify: `src/main/kotlin/uk/gov/communities/prsdb/webapp/config/security/DefaultSecurityConfig.kt`

**Step 1: Update CSP directives to remove GA/GTM URLs**

Replace the `CONTENT_SECURITY_POLICY_DIRECTIVES` constant:

```kotlin
const val CONTENT_SECURITY_POLICY_DIRECTIVES =
    "default-src 'self'; " +
        "script-src 'self' 'nonce-' $PLAUSIBLE_URL; " +
        "connect-src 'self' $PLAUSIBLE_URL; " +
        "img-src 'self'; " +
        "style-src 'self'; " +
        "object-src 'none'; base-uri 'none'; frame-ancestors 'none';"
```

Changes:
- `script-src`: removed `$GOOGLE_TAG_MANAGER_URL`
- `connect-src`: removed `$REGION_1_GOOGLE_ANALYTICS_URL $GOOGLE_TAG_MANAGER_URL $GOOGLE_URL`
- `img-src`: removed `$GOOGLE_TAG_MANAGER_URL`

**Step 2: Remove unused imports**

Remove these imports:

```kotlin
import uk.gov.communities.prsdb.webapp.constants.GOOGLE_TAG_MANAGER_URL
import uk.gov.communities.prsdb.webapp.constants.GOOGLE_URL
import uk.gov.communities.prsdb.webapp.constants.REGION_1_GOOGLE_ANALYTICS_URL
```

---

### Task 5: Remove GA scripts and cookie banner from layout.html

**Files:**
- Modify: `src/main/resources/templates/fragments/layout.html`

**Step 1: Remove GA-related Thymeleaf variable declarations**

Remove these comment lines from the top of the file:

```html
<!--/*@thymesVar id="googleAnalyticsMeasurementId" type="String"*/-->
<!--/*@thymesVar id="googleTagManagerUrl" type="java.util.List<String>"*/-->
```

**Step 2: Remove all 4 GA script blocks**

Remove the entire block between `</head>` and `<body>` — these 4 script elements:

```html
<script th:nonce="${serverGeneratedNonce}">
    <!-- Set consent for Google tag to denied by default -->
    window.dataLayer = window.dataLayer || [];
    function gtag() { dataLayer.push(arguments); }
    gtag('consent', 'default', {
        'ad_user_data': 'denied',
        'ad_personalization': 'denied',
        'ad_storage': 'denied',
        'analytics_storage': 'denied',
        'wait_for_update': 500,
    });
</script>
<script async th:src="${googleTagManagerUrl}+${googleAnalyticsMeasurementId}"></script>
<script th:nonce="${serverGeneratedNonce}" th:inline="javascript" th:with="measurementIdAsString=${googleAnalyticsMeasurementId}">
    <!-- Google tag (gtag.js) -->
    window.dataLayer = window.dataLayer || [];
    function gtag() { dataLayer.push(arguments); }
    gtag('js', new Date());
    gtag('config', [[${measurementIdAsString}]])
</script>
<script th:nonce="${serverGeneratedNonce}" th:inline="javascript">
    // Add environment variables to the window so they can be accessed in js scripts
    window.GOOGLE_ANALYTICS_MEASUREMENT_ID = [[${googleAnalyticsMeasurementId}]];
    window.GOOGLE_COOKIE_DOMAIN = [[${googleAnalyticsCookieDomain}]];
</script>
```

**Step 3: Remove cookie banner include**

Remove this line from `<body>`:

```html
<div th:replace="~{ fragments/banners/cookieBanner:: cookieBanner }"></div>
```

The resulting layout should have `<body>` start directly with the skip link.

---

### Task 6: Delete cookie consent JavaScript files

**Files:**
- Delete: `src/main/js/cookieConsentHandler.js`
- Delete: `src/main/js/googleAnalyticsSetUp.js`
- Modify: `src/main/js/index.js`

**Step 1: Delete cookieConsentHandler.js**

Delete the file entirely.

**Step 2: Delete googleAnalyticsSetUp.js**

Delete the file entirely.

**Step 3: Remove the import and call from index.js**

Remove these lines from `src/main/js/index.js`:

```javascript
import {addCookieConsentHandler} from "./cookieConsentHandler.js";
```

```javascript
addCookieConsentHandler()
```

The resulting `index.js` should be:

```javascript
import {initAll as initGDS} from 'govuk-frontend';
import {initSelectAutocomplete} from "./autocomplete";
import {addFileUploadListener} from "./fileUploadScript";
import $ from 'jquery'
import {initAll as initMoJDS} from '@ministryofjustice/frontend'
import {initFilterToggleButton} from "./filterToggleButton"
import '../resources/css/custom.scss'
import {setJsEnabled} from "#main-javascript/setJsEnabled.js";

setJsEnabled()

initGDS()

initSelectAutocomplete()

addFileUploadListener()

window.$ = $
initMoJDS()
initFilterToggleButton()
```

---

### Task 7: Update cookies page template and messages

**Files:**
- Modify: `src/main/resources/templates/cookies.html`
- Modify: `src/main/resources/messages/cookies.yml`

**Step 1: Remove analytics section, consent form, and noscript from cookies.html**

Remove:
- The entire `<section>` containing analytics heading, paragraphs, bullets, and table (the one with `id="analytics-tbody"`)
- The entire `<form>` element (consent radio buttons and submit button)
- The entire `<noscript>` block
- The success banner (no longer needed without the consent form)

Also remove `cookies.paragraph.two` and `cookies.paragraph.three` references from the header paragraphs (they reference analytics cookies). Keep `cookies.paragraph.one` which describes cookies in general.

Add an HTML comment: `<!-- TODO: PDJB-727 Revisit this page with design once GA removal is confirmed -->`

**Step 2: Update cookies.yml**

Remove the following sections:
- `analytics` (entire section: heading, paragraphs, bullets, table)
- `fieldSetHeading`
- `button`
- `noScript` (entire section)
- `successBanner` (entire section)
- `paragraph.two` (references analytics cookies)
- `paragraph.three` (references analytics cookies)

Keep:
- `title`
- `heading`
- `paragraph.one` (general cookies explanation)
- `essential` (entire section)
- `table` (heading definitions for Name/Purpose/Expires)

Remove the `cookie_consent` row from the essential cookies table — that cookie will no longer be set.

---

### Task 8: Delete cookie banner template and messages

**Files:**
- Delete: `src/main/resources/templates/fragments/banners/cookieBanner.html`
- Delete: `src/main/resources/messages/cookieBanner.yml`

**Step 1: Delete both files**

These are no longer referenced from layout.html.

---

### Task 9: Remove the `cookie` npm dependency

**Files:**
- Modify: `package.json`

**Step 1: Check if `cookie` is used elsewhere**

The `cookie` npm package was imported only by `cookieConsentHandler.js`. Search for other usages of `import * as cookieHelper from 'cookie'` or `require('cookie')` in `src/main/js/`. If no other files use it, uninstall it:

```shell
npm uninstall cookie
```

---

### Task 10: Delete JS unit tests for cookie consent handler

**Files:**
- Delete: `src/test/js/cookieConsentHandler.test.js`

**Step 1: Delete the file**

All tests in this file test GA consent logic and cookie banner behaviour — none are relevant after removal.

---

### Task 11: Delete cookie banner integration tests and page objects

**Files:**
- Delete: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/CookieBannerTests.kt`
- Delete: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/CookiesPageTests.kt`
- Delete: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/components/CookieBanner.kt`
- Modify: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/pages/CookiesPage.kt`
- Modify: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/pages/LandlordDashboardPage.kt`
- Modify: `src/test/kotlin/uk/gov/communities/prsdb/webapp/integration/pageObjects/pages/LandlordDetailsPage.kt`

**Step 1: Delete CookieBannerTests.kt**

All tests are for the cookie banner which no longer exists.

**Step 2: Delete CookiesPageTests.kt**

All tests are for the consent form which no longer exists. The `CookiesControllerTests.kt` (basic GET route tests) should remain — the cookies page still exists.

**Step 3: Delete CookieBanner.kt page object**

No longer needed.

**Step 4: Update CookiesPage.kt page object**

Remove the `consentForm` property and `ConsentForm` inner class. Remove the `successBanner` property and `SuccessBanner` inner class. Keep `backLink` and `heading`.

Updated file:

```kotlin
package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CookiesController.Companion.COOKIES_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CookiesPage(
    page: Page,
) : BasePage(page, COOKIES_ROUTE) {
    val backLink = BackLink.default(page)
    val heading = Heading(page.locator("main h1"))
}
```

**Step 5: Remove cookieBanner from LandlordDashboardPage.kt**

Remove the `cookieBanner` property and the `CookieBanner` import.

**Step 6: Remove cookieBanner from LandlordDetailsPage.kt**

Remove the `cookieBanner` property and the `CookieBanner` import.

**Step 7: Check for other pages that reference CookieBanner**

Search for `CookieBanner` imports across all page object files. Remove any remaining references.

---

### Task 12: Update GlobalModelAttributes to remove cookiesUrl if unused

**Files:**
- Investigate: `src/main/resources/templates/fragments/footer.html`
- Possibly modify: `src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/controllerAdvice/GlobalModelAttributes.kt`

**Step 1: Check if the footer uses `cookiesUrl` model attribute**

Look at the footer template to see how the cookies link is constructed. If it uses a hardcoded path or `@{/cookies}` rather than `${cookiesUrl}`, then the `cookiesUrl` model attribute (which includes back-link override logic for the banner) can be removed from `GlobalModelAttributes`.

If removed, also remove the import of `COOKIES_ROUTE` and `BackUrlStorageService` usage for it.

---

### Task 13: Build and verify

**Step 1: Build the Rollup bundle**

```shell
npm run build
```

Verify no errors from the JS bundling.

**Step 2: Build the Gradle project**

```shell
./gradlew build -x test
```

Verify no compilation errors.

**Step 3: Run unit tests (excluding integration)**

```shell
./gradlew testWithoutIntegration --console=plain
```

Verify all tests pass.

**Step 4: Run JS tests**

```shell
npm test
```

Verify all remaining JS tests pass (cookieConsentHandler.test.js should be gone).

---

## PR Breakdown

**PR 1 (only PR) — Remove Google Analytics and cookie consent banner**

This is a single PR because:
- All changes are tightly coupled: GA code, the cookie consent banner, and the consent handler form a single integrated feature
- Splitting would create broken intermediate states (e.g., removing GA scripts but keeping the consent handler that references `window.GOOGLE_ANALYTICS_MEASUREMENT_ID`)
- The total diff is mostly deletions, making it easy to review in one pass
- Each "task" above maps to a logically distinct file group, so the PR is still easy to review by file section

**Scope:** Tasks 1–13 (all tasks)
**Excludes:** Privacy notice updates (legal team), ADR updates, deployment env var removal (ops)
