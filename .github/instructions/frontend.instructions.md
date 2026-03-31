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
```html
<!DOCTYPE html>
<html th:replace="~{layout :: layout(~{::main}, ~{::title})}">
<head><title>Page Title - Service Name</title></head>
<body>
<main>
    <!-- Page content here -->
</main>
</body>
</html>
```

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
