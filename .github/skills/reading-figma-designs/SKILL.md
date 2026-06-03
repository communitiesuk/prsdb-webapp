---
name: reading-figma-designs
description: Use when interpreting Figma designs into implementation requirements. Covers extracting design tokens, mapping to GOV.UK components, and producing implementation-ready specs.
---

# Reading Figma Designs

Instructions for interpreting Figma MCP output into implementation requirements.

## Prerequisites

- Figma MCP server must be connected (check via preflight activation checks)
- The user must provide a Figma file URL or have the relevant frame selected in the
  Figma desktop app

## Process

### 1. Navigate to the Correct Frame

Use Figma MCP tools to locate the relevant design:
- If a URL is provided, navigate directly to that frame
- If no URL, ask the user to select the relevant layer/screen in the Figma desktop
  app — the MCP server can read the current selection

### 2. Extract Design Tokens

For each component or section in the design, extract:
- **Spacing:** margins, padding (map to GOV.UK spacing scale: 0-9)
- **Typography:** font size, weight, line height (map to GOV.UK typography classes)
- **Colours:** hex values (map to GOV.UK colour palette variables)
- **Layout:** grid structure, column widths, responsive breakpoints

### 3. Map to GOV.UK Design System Components

Identify which GOV.UK Design System components match the design:
- Standard components: buttons, inputs, radios, checkboxes, panels, tables
- MOJ Frontend components: multi-select, sub-navigation, timeline
- Custom components: identify where bespoke markup is needed

Common mappings:

| Figma Pattern | GOV.UK Component |
|---------------|-----------------|
| Text input with label above | `govuk-input` with `govuk-label` |
| Radio buttons in a fieldset | `govuk-radios` with `govuk-fieldset` |
| Green confirmation panel | `govuk-panel--confirmation` |
| Summary list (key-value pairs) | `govuk-summary-list` |
| Table with sortable columns | `moj-sortable-table` |
| Tabs with content panels | `govuk-tabs` |
| Warning text with icon | `govuk-warning-text` |
| Inset text (quoted/highlighted) | `govuk-inset-text` |
| Details (expandable) | `govuk-details` |
| Notification banner | `govuk-notification-banner` |
| Tag (status label) | `govuk-tag` |
| Task list | `govuk-task-list` |

### 4. Handle Responsive Variants

If the design includes mobile/tablet variants:
- Note breakpoint-specific layout changes
- Identify components that stack vs remain side-by-side
- Check for mobile-specific content visibility

### 5. Produce Implementation Spec

Output a structured summary for each page/component:
- Template path (based on project conventions: `src/main/resources/templates/`)
- GOV.UK components to use (with specific variants)
- Content text (exact copy from the design)
- Conditional visibility rules (if elements show/hide based on state)
- Form field names and types (if the page contains a form)
- Validation requirements visible in the design (error states, hint text)

## GOV.UK Design System Reference

This project uses:
- GOV.UK Frontend 5.11.0
- Ministry of Justice Frontend 3.3.1
- Thymeleaf templates with fragment composition

Template conventions:
- Layouts extend `layout.html`
- Fragments are in `components/` subdirectories
- Form pages use `formPageWrapper.html` fragment
- Page titles follow `<page> - <service> - GOV.UK` pattern

## Taking Screenshots for Validation

When extracting designs, also capture screenshots of the relevant Figma frames. These
screenshots serve as reference images during implementation validation — allowing
visual comparison between the design and the running application.

- Take a screenshot of each distinct page or state shown in the design
- Name screenshots clearly (e.g. `landlord-dashboard.png`, `registration-step-3-error.png`)
- Store screenshots in the session workspace for later use during smoke testing and
  verification phases

## Output Format

Present findings as a structured list the implementing agent can act on directly:

```
Page: [name]
Template: src/main/resources/templates/[path].html
Layout: formPageWrapper / contentPage / taskList (etc.)

Components:
- govuk-heading-xl: "[heading text]"
- govuk-body: "[body text]"
- govuk-radios:
  - name: [field name]
  - items: [list of options from design]
  - hint: "[hint text if present]"

Conditional elements:
- [element]: shown when [condition]

Validation (visible in design):
- [field]: [error message text shown in error state]
```
