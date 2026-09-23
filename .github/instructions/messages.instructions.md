---
applyTo: "**/messages/**"
---

# Messages / i18n Instructions

## File Location and Structure

Message files are YAML files in `src/main/resources/messages/`, organised by feature or page:
- `common.yml`, `commonText.yml`, `default.yml` — shared messages
- Feature-specific files: `registerProperty.yml`, `landlord.yml`, `propertyCompliance.yml`, `error.yml`, etc.

`MessageSourceConfig` installs the custom `YamlMessageSource`, which loads top-level `*.yml` files from this
directory. This is project-specific support, not Spring Boot's default message-file loading; nested directories
and `.yaml` files are not included by that resource pattern.

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
- Duplicate resolved keys across files cause an `IllegalStateException`; they do not silently override one another.

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
      one: We’ve sent you an email...

# In default.yml — keys are used as-is, no prefix
serviceName: Register your rental property
sectionHeader: 'Section {0,,sectionNumber} of {1,,totalSections} — {2,,sectionName}'

# In commonText.yml — keys are auto-prefixed with "commonText."
'yes': 'Yes'
'no': 'No'
```

## Message Type Patterns

| Type | Pattern | Example |
|------|---------|---------|
| Page titles | `{feature}.title` | `registerProperty.title` |
| Page headings | `{feature}.heading` | `registerProperty.heading` |
| Error messages | `{feature}.{section}.{element}` | `error.notFound.header`, `forms.email.error.missing` |
| Form labels | `forms.{formName}.{fieldName}` or the feature's namespace | `forms.yml` supplies `forms.*`; `form.yml` supplies `form.*` |
| Common UI | `common.{element}` | `common.confirmationPage.whatHappensNext` |
| Parameterised | Positional `{0}` or labelled `{0,,paramName}` | `sectionHeader` in `default.yml` |

Both parameter styles use positional arguments; the label in `{0,,paramName}` is not a named-argument lookup.
Quote keys such as `'yes'` and `'no'` so YAML does not interpret them as booleans.

## Numbered Content Blocks

For ordered multi-paragraph or bulleted content, numbered keys are common:

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

Descriptive keys are also established, for example `registerProperty.whoProvidesRentalDetails.bullet.licensing`.
Follow the surrounding feature's structure rather than renumbering existing semantic keys.

## Template Usage

```html
<!-- Simple message lookup -->
<h2 th:text="#{common.confirmationPage.whatHappensNext}">common.confirmationPage.whatHappensNext</h2>

<!-- With parameters -->
<h1 th:text="${#messages.msgWithParams(contentHeader, contentHeaderParams)}">contentHeader</h1>

<!-- Safe lookup with fallback -->
<td th:text="${#messages.msgOrNull(column.fieldValue)} ?: ${{column.fieldValue}}">column.fieldValue</td>
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

Curly apostrophes (`’` U+2019) are **not** special characters in YAML — they can be used directly in unquoted string values. **Do not** wrap a value in double quotes or use `\u2019` unicode escapes just to include a curly apostrophe.

## Adding New Messages

1. Identify the appropriate YAML file (match to feature/page, or create a new file if needed)
2. Follow the existing key hierarchy in that file
3. Use the `{feature}.{section}.{element}` naming pattern
4. For parameterised messages, use positional placeholders and match the argument order at every call site
5. Use curly apostrophes (`’`) in message text, not straight apostrophes (`'`)
