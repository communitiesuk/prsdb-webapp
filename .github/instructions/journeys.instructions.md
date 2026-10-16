---
applyTo: "**/journeys/**,**/forms/**"
---

# Journey Framework Instructions

## Documentation Reference
- Full framework docs: [docs/JourneyFrameworkReadMe.md](../../docs/JourneyFrameworkReadMe.md)
- Detailed guides in `src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/docs/`:
  - `JourneyFrameworkReadMe.md` — overview
  - `JourneyFrameworkModel.md` — data model
  - `JourneyFrameworkCreateAJourney.md` — how to create a journey
  - `JourneyFrameworkExtendTheFramework.md` — extending the framework

## Key Patterns

### Journey Structure
Journey class hierarchy:
```
Journey<T: StepId>
├── JourneyWithTaskList<T: StepId>
├── UpdateJourney<T: StepId>
│   └── GroupedUpdateJourney<T: GroupedUpdateStepId<*>>
```
- Steps are defined as enum values unique to each journey
- `GroupedUpdateJourney` groups steps into sections with per-group check-your-answers pages (e.g., property compliance updates for EPC, EICR, gas safety separately)
- Use factories to instantiate journeys (see [Factory Pattern](#factory-pattern) below)

### Step Definition
```kotlin
Step(
    id = StepId.EXAMPLE_STEP,
    page = ExamplePage::class,
    nextAction = { journeyData -> StepId.NEXT_STEP },
    handleSubmitAndRedirect = { /* optional: for completion or skip logic */ },
    saveAfterSubmit = true  // persist progress to DB
)
```

### Factory Pattern
Journey factories are annotated with `@PrsdbWebComponent` or `@PrsdbWebService` and handle journey instantiation with all dependencies:
```kotlin
@PrsdbWebComponent
class PropertyComplianceJourneyFactory(
    private val validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    // domain-specific services
) {
    fun create(stepName: String, propertyOwnershipId: Long) = PropertyComplianceJourney(...)
}
```

### Page Titles
Each step's `<title>` is built from its h1 by the layout (see `frontend.instructions.md`). Step templates pass their
h1 expression as the layout `title` argument.

Never set a journey-wide `"title"` content property (e.g. `withAdditionalContentProperty { "title" to ... }` on a
whole journey, task or `configure { }` block): every step would share one title instead of using its own h1.

Where a single step on a shared form template needs a title different from its h1, set a `"title"` content property
(a `pageTitle` message key) on that step only — via `withAdditionalContentProperty` on the step in the journey
factory, or in the step config's `getStepSpecificContent` if every use of that step needs it, e.g.
`"title" to "uploads.checkUploads.gasSafety.pageTitle"` in `CheckGasCertUploadsStepConfig`. Have the template fall
back to its h1 expression: `#{${title ?: addAnotherTitle}(${optionalAddAnotherTitleParam})}`. Only add the fallback
to templates that have an override, and override `expectedTitleHeading` on the step's page object.

### Transaction Metric Tagging
A journey's final commit step must render its submit button via `transactionSubmitButton` (or `transactionWarningButton` for destructive actions) so the completion is counted in the transaction metric — either hardcoded in a commit-only template, or opted into with `"submitButton" to "transactionSubmitButton"` in `withAdditionalContentProperties` where the template is shared with non-commit steps. Tag exactly one button per completed journey; where that is not possible, record the gap in [MetricsReadMe](../../docs/MetricsReadMe.md) rather than risk double-counting.

### Page Class Conventions
Page class hierarchy:
```
AbstractPage (abstract base)
├── Page (standard pages)
├── PageWithContentProvider (dynamic content via lambda)
├── FileUploadPage (file upload with custom validation)
├── UnvisitablePage (non-navigable marker pages)
├── CheckAnswersPage (abstract, displays submitted data as summary)
│   └── BasicCheckAnswersPage (abstract, implements getSummaryList())
```
Key methods on `AbstractPage`: `getModelAndView()`, `enrichModel()`, `bindDataToFormModel()`, `isSatisfied()`

### Navigation Flow
- `nextAction`: Returns next step based on journey data (can be conditional)
- `handleSubmitAndRedirect`: Used for final submission or skipping to check-answers
- Steps must be reachable via the iterator to be accessible

### Tasks and Sections
- Group steps into `Task` objects for task-list display
- Group tasks into sections for logical organization
- Task status is derived from step completion state

### Testing
- Controller tests verify step rendering and submission
- Journey tests verify navigation flow and data persistence
- Use existing journey tests as templates
