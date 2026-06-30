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
