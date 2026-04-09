---
applyTo: "**/messages/**"
---

# Messages / i18n Instructions

## File Location and Structure

Message files are YAML files in `src/main/resources/messages/`, organised by feature or page:
- `common.yml`, `commonText.yml`, `default.yml` — shared messages
- Feature-specific files: `registerProperty.yml`, `landlord.yml`, `propertyCompliance.yml`, `error.yml`, etc.

Spring Boot auto-loads all YAML files from this directory.

## Key Naming Convention

Use hierarchical dot notation: `{feature}.{section}.{element}.{variant}`

```yaml
# Page-level keys
registerProperty.title: Register a Property
registerProperty.heading: Register a property

# Nested sections
registerProperty.confirmation.banner.heading: You have registered a property
registerProperty.confirmation.whatHappensNext.paragraph.one: We’ve sent you an email...

# Error messages
notFound.title: 'Page not found - {0,,serviceName} - GOV.UK'
notFound.header: Page not found

# Common shared text
commonText.yes: 'Yes'
commonText.no: 'No'
```

## Message Type Patterns

| Type | Pattern | Example |
|------|---------|---------|
| Page titles | `{feature}.title` | `registerProperty.title` |
| Page headings | `{feature}.heading` | `registerProperty.heading` |
| Error messages | `{errorType}.{component}` | `notFound.header` |
| Form labels | `forms.{formName}.{fieldName}` | In feature YAML or `form.yml` |
| Common UI | `common.{element}` | `common.confirmationPage.whatHappensNext` |
| Parameterised | `{0,,paramName}` syntax | `You have {0,,number} outstanding actions` |

## Numbered Content Blocks

For multi-paragraph or bulleted content, use numbered keys:

```yaml
whatHappensNext:
  paragraph:
    one: First paragraph
    two: Second paragraph
  bullet:
    heading: 'You will need to:'
    one: First item
    two: Second item
```

## Template Usage

```html
<!-- Simple message lookup -->
<h2 th:text="#{common.confirmationPage.whatHappensNext}">Fallback</h2>

<!-- With parameters -->
<h1 th:text="${#messages.msgWithParams(contentHeader, contentHeaderParams)}">Fallback</h1>

<!-- Safe lookup with fallback -->
<td th:text="${#messages.msgOrNull(column.fieldValue)} ?: ${{fieldValue}}">Fallback</td>
```

## Apostrophes

Always use **curly (typographic) apostrophes** (`’` U+2019) in message text, not straight apostrophes (`'` U+0027).

```yaml
# Correct — curly apostrophe
registerProperty.confirmation.whatHappensNext.paragraph.one: We’ve sent you an email...

# Wrong — straight apostrophe
registerProperty.confirmation.whatHappensNext.paragraph.one: We've sent you an email...
```

This applies to all human-readable message values (headings, body text, error messages, etc.). Straight apostrophes should only appear as YAML syntax (e.g. quoting strings).

## Adding New Messages

1. Identify the appropriate YAML file (match to feature/page, or create a new file if needed)
2. Follow the existing key hierarchy in that file
3. Use the `{feature}.{section}.{element}` naming pattern
4. For parameterised messages, use `{0,,paramName}` syntax
5. Use curly apostrophes (`’`) in message text, not straight apostrophes (`'`)
