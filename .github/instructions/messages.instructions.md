---
applyTo: "**/messages/**"
---

# Messages / i18n Instructions

## File Location and Structure

Message files are YAML files in `src/main/resources/messages/`, organised by feature or page:
- `common.yml`, `commonText.yml`, `default.yml` — shared messages
- Feature-specific files: `registerProperty.yml`, `landlord.yml`, `propertyCompliance.yml`, `error.yml`, etc.

Spring Boot auto-loads all YAML files from this directory.

## Filename-Based Key Prefixing

The custom `YamlMessageSource` automatically prefixes all message keys with the YAML filename, **except** for
`default.yml` whose keys are used as-is.

For example, if `registerProperty.yml` contains:

```yaml
title: Register a Property
confirmation:
  banner:
    heading: You have registered a property
```

The resolved message keys are `registerProperty.title` and `registerProperty.confirmation.banner.heading` — the
filename `registerProperty` is prepended automatically. You do **not** need to repeat the filename inside the YAML
structure.

Keys in `default.yml` have no prefix applied, so they are resolved exactly as written in the file.

This means:
- The filename you choose for a new YAML file determines the top-level prefix for all keys in that file.
- Moving a key from one file to another changes its resolved message key.

## Key Naming Convention

Use hierarchical dot notation: `{feature}.{section}.{element}.{variant}`

```yaml
# In registerProperty.yml — keys are auto-prefixed with "registerProperty."
title: Register a Property
heading: Register a property

# Nested sections (in registerProperty.yml)
confirmation:
  banner:
    heading: You have registered a property
  whatHappensNext:
    paragraph:
      one: We've sent you an email...

# In default.yml — keys are used as-is, no prefix
notFound.title: 'Page not found - {0,,serviceName} - GOV.UK'
notFound.header: Page not found

# In commonText.yml — keys are auto-prefixed with "commonText."
yes: 'Yes'
no: 'No'
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
<h2 th:text="#{common.confirmationPage.whatHappensNext}">common.confirmationPage.whatHappensNext</h2>

<!-- With parameters -->
<h1 th:text="${#messages.msgWithParams(contentHeader, contentHeaderParams)}">contentHeader</h1>

<!-- Safe lookup with fallback -->
<td th:text="${#messages.msgOrNull(column.fieldValue)} ?: ${{fieldValue}}">fieldValue</td>
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

Curly apostrophes (`'` U+2019) are **not** special characters in YAML — they can be used directly in unquoted string values. **Do not** wrap a value in double quotes or use `\u2019` unicode escapes just to include a curly apostrophe.

## Adding New Messages

1. Identify the appropriate YAML file (match to feature/page, or create a new file if needed)
2. Follow the existing key hierarchy in that file
3. Use the `{feature}.{section}.{element}` naming pattern
4. For parameterised messages, use `{0,,paramName}` syntax
5. Use curly apostrophes (`’`) in message text, not straight apostrophes (`'`)
