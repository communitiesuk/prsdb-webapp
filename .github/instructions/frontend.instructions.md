---
applyTo: "**/templates/**,**/css/**,**/js/**,**/assets/**,rollup.config.mjs,package.json,package-lock.json"
---

# Frontend Instructions

## Technology Stack
- **Templating**: Thymeleaf with GOV.UK Design System
- **CSS**: SASS/SCSS compiled via Rollup
- **JS**: JavaScript modules bundled via Rollup, with GOV.UK/MoJ initialisers and jQuery for MoJ
- **Design System**: GOV.UK Frontend and Ministry of Justice Frontend; use `package.json` and `package-lock.json` for versions

## Thymeleaf Templates

### Layout Structure
Every page uses `fragments/layout :: layout(title, content, hasErrors)`:
```html
<!DOCTYPE html>
<html th:replace="~{fragments/layout :: layout(#{lettingAgentInvitation.invalidLink.heading}, ~{::main}, false)}">
<main class="govuk-main-wrapper">
    <div th:replace="~{fragments/layouts/twoThirdsLayout :: twoThirdsLayout(~{::#page-content})}">
        <div id="page-content">
            <h1 class="govuk-heading-l" th:text="#{lettingAgentInvitation.invalidLink.heading}">
                lettingAgentInvitation.invalidLink.heading
            </h1>
        </div>
    </div>
</main>
</html>
```

Based on `invalidLettingAgentInvitationLink.html`. Use `twoThirdsLayout` or `fullWidthLayout` for the content width,
rather than duplicating their grid markup.

### Page Titles
The layout builds the `<title>` as `[Error: ]<title> - <service name> - GOV.UK` (via the `pageTitleFormat` message), so
each page title is unique and descriptive:
- Pass the **same expression as the page's h1** as the `title` argument.
- Only use a different title when the h1 is unsuitable as a title on its own. To be a suitable title, the h1 should:
  - not be too long. Aim for 65 characters or fewer, but this is a guideline: a slightly longer h1 is fine if a
    shorter title would be less clear.
  - be unique. Different pages a user can reach should not share a title, e.g. a shared template used by both the gas
    and electrical safety tasks. Mutually exclusive variants of one step (e.g. occupied/unoccupied versions) can share
    a title, as a user only sees one of them.
  - describe the page without its surrounding content, e.g. a check answers page whose h1 is just the topic
    ("Gas safety certificate") is not descriptive enough.
- If the h1 fails these checks, pass a different message (usually a `pageTitle` key alongside the `heading` key) as
  the `title` argument, but this should be rare and should always be checked first. For shared journey form templates
  where only some steps need a different title, see `pageTitleOverride` in `journeys.instructions.md`.
- The `Error: ` prefix is added automatically when `hasErrors` is true.
- Many existing templates still pass a shared `{feature}.title` message while page titles are being migrated; do not
  copy that pattern into new or changed pages.

### Using Fragments
```html
<div th:replace="~{fragments/forms/basicTextInput :: textInput(#{forms.name.label}, 'name', null)}"></div>

<button th:replace="~{fragments/buttons/primaryButton :: primaryButton(#{forms.buttons.continue})}"></button>
```

Input fragments belong inside a form bound to its form model. The fragment name need not match the filename:
`basicTextInput.html`, for example, exports `textInput(label, fieldName, hint)`.

### Form Pages
- Reuse `fragments/forms/questionPage` for ordinary questions and `complexQuestionPage` when introductory content
  sits outside the form. See `forms/nameForm.html` and `forms/rentAmountForm.html`.
- These wrappers supply the layout, back link, error summary, section header and POST form bound with `th:object`.
  Keep `th:action`, server-side validation and Spring's Thymeleaf CSRF integration when composing forms.
- Reuse fieldsets and input fragments for `th:field`, error classes, labels, hints and accessible relationships.
  Use public input fragments, not `innerTextInput` directly. Radio options can supply conditional fragments.
- Preserve existing `@thymesVar` declarations and `data-testid` hooks when changing shared templates.

### Form Fragments Location
- `templates/fragments/forms/` - Input components (text, date, checkbox, etc.)
- `templates/fragments/buttons/` - Button variants
- `templates/fragments/banners/` - Notification and confirmation banners
- `templates/fragments/layouts/` - Page layout variants
- `templates/fragments/pagination/` - Pagination controls
- `templates/fragments/tabs/` - Tab components
- `templates/fragments/tables/` - Table components
- `templates/fragments/header/` - Page header variants
- `templates/fragments/content/` - Content blocks (confirmation pages, guidance)
- `templates/fragments/conditional/` - Conditional input fields (custom property type, rent frequency, bills)
- `templates/fragments/taskList/` - Task list components

### Using Messages
Messages are defined in YAML files under `src/main/resources/messages/` (see `messages.instructions.md` for full
details). Reference them in templates with the `#{...}` syntax:

```html
<h1 th:text="#{registerProperty.heading}">registerProperty.heading</h1>
<p class="govuk-body" th:text="#{betaBannerFeedback.intro}">betaBannerFeedback.intro</p>
```

**Use the message key as the placeholder text.** The static content between the tags should be the same message key
you pass to `th:text`. This is the established convention across the project.

For parameterised messages, use a message expression or the `#messages` helper for a parameter array:

```html
<h1 th:text="${#messages.msgWithParams(contentHeader, contentHeaderParams)}">contentHeader</h1>
```

Use escaped `th:text` by default. Existing `th:utext` is for deliberately authored message markup; do not use it
to render untrusted input as HTML.

## GOV.UK Design System
- Follow [GOV.UK Design System](https://design-system.service.gov.uk/) patterns
- Use standard class names: `govuk-*`
- Use correct typography: `govuk-heading-l`, `govuk-body`, etc.

## Custom SCSS
- Location: `src/main/resources/css/`
- Register partials with `@use` in `custom.scss`; reuse GOV.UK spacing, colour and typography helpers
- Prefix custom classes to avoid conflicts

```scss
// _example.scss
.prsdb-custom-component {
    // styles
}
```

## JavaScript
- Location: `src/main/js/`
- Entry point: `src/main/js/index.js`
- Export component initialisers and invoke them from the entry point alongside the existing GOV.UK/MoJ setup
- Use progressive enhancement (JS enhances, doesn't require)
- Inline scripts must carry `th:nonce="${serverGeneratedNonce}"`, as in `fragments/layout.html`
- Preserve analytics query-string stripping and the distinct Flow/Transaction events; see
  [AnalyticsReadMe](../../docs/AnalyticsReadMe.md)

### Transaction Buttons
Use `transactionSubmitButton` or `transactionWarningButton` only for the journey's final commit submission.
Shared forms opt in through the `submitButton` content property and otherwise use `primaryButton`.
Follow the exactly-once and intentional coverage-gap rules in [journeys.instructions.md](journeys.instructions.md);
do not tag intermediate or cancellation-choice buttons merely because they submit a form.

## Static Assets
- Rollup bundles JS/SCSS and copies configured dependency assets into `dist/`
- Gradle copies `dist/` into `build/resources/main/static/assets/`; neither location is source to edit
- There is no automatic copy from `src/main/resources/assets/`; add any new copy inputs explicitly to Rollup
- Reference classpath static assets using Thymeleaf URLs, for example `th:src="@{/assets/js/index.js}"`
- See [build-ci.instructions.md](build-ci.instructions.md) for the Gradle dependency chain

## Build Commands
```powershell
npm run build      # Build frontend assets
npm test           # Run frontend tests
```

Frontend tests live in `src/test/js/` and use `node:test`, Node assertions and, where DOM behaviour is needed,
`global-jsdom`. Match the existing tests rather than applying Kotlin/JUnit conventions to these files.
