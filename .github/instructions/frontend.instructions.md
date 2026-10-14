---
applyTo: "**/templates/**,**/css/**,**/js/**,**/assets/**"
---

# Frontend Instructions

## Technology Stack
- **Templating**: Thymeleaf with GOV.UK Design System
- **CSS**: SASS/SCSS compiled via Rollup
- **JS**: Vanilla JavaScript bundled via Rollup
- **Design System**: GOV.UK Frontend 5.11.0 + Ministry of Justice Frontend 3.3.1

## Thymeleaf Templates

### Layout Structure
Every page uses `fragments/layout :: layout(title, content, hasErrors)`:
```html
<!DOCTYPE html>
<html th:replace="~{fragments/layout :: layout(#{example.heading}, ~{::main}, false)}">
<main>
    <h1 class="govuk-heading-l" th:text="#{example.heading}">Example heading</h1>
    <!-- Page content here -->
</main>
</html>
```

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

### Using Fragments
```html
<!-- Include a fragment -->
<div th:replace="~{fragments/forms/basicTextInput :: basicTextInput(...)}"></div>

<!-- Include with parameters -->
<div th:replace="~{fragments/buttons/primary :: primaryButton(text='Continue')}"></div>
```

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

For parameterised messages, use the `#messages` helper:

```html
<h1 th:text="${#messages.msgWithParams(contentHeader, contentHeaderParams)}">contentHeader</h1>
```

## GOV.UK Design System
- Follow [GOV.UK Design System](https://design-system.service.gov.uk/) patterns
- Use standard class names: `govuk-*`
- Use correct typography: `govuk-heading-l`, `govuk-body`, etc.

## Custom SCSS
- Location: `src/main/resources/css/`
- Import in `custom.scss`
- Prefix custom classes to avoid conflicts

```scss
// _example.scss
.prsdb-custom-component {
    // styles
}
```

## JavaScript
- Location: `src/main/resources/js/`
- Entry point: `src/main/js/index.js`
- Use progressive enhancement (JS enhances, doesn't require)

## Static Assets
- Place in `src/main/resources/assets/`
- Copied to `static/assets/` at build time
- Reference as `/assets/filename.ext`

## Build Commands
```powershell
npm run build      # Build frontend assets
npm test           # Run frontend tests
```
