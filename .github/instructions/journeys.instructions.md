---
applyTo: "**/journeys/**/*.kt,**/annotations/webAnnotations/JourneyFrameworkComponent.kt"
---

# Journey Framework Instructions

## Documentation Reference
- Current framework overview: [journeys/docs/JourneyFrameworkReadMe.md](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/docs/JourneyFrameworkReadMe.md)
- Detailed guides in `src/main/kotlin/uk/gov/communities/prsdb/webapp/journeys/docs/`:
  - `JourneyFrameworkReadMe.md` — overview
  - `JourneyFrameworkModel.md` — data model
  - `JourneyFrameworkCreateAJourney.md` — how to create a journey
  - `JourneyFrameworkExtendTheFramework.md` — extending the framework

Use current Kotlin source for API examples: the root `docs/JourneyFrameworkReadMe.md` describes the old framework, and the create-a-journey guide's internal-step example is also stale.

## Key Patterns

### Journey Structure
- Build a directed graph with `JourneyBuilder.journey(state) { ... }`, returning `Map<String, StepLifecycleOrchestrator>` keyed by requestable step paths
- Route segments are strings/constants such as `BedroomsStep.ROUTE_SEGMENT`; mode enums such as `Complete` describe outcomes, not step identifiers
- Both flag-off and restructured property registration use this DSL; the flag-off flow is not the old framework
- Use factories to initialise state and build routing maps (see [Factory Pattern](#factory-pattern) below)

### Step Definition
- `JourneyStep.RequestableStep<Mode, FormModel, State>` wraps an `AbstractRequestableStepConfig<Mode, FormModel, State>` for a page
- `JourneyStep.InternalStep<Mode, State>` wraps an `AbstractInternalStepConfig<Mode, State>` for logic without a directly requestable URL

From `propertyRegistration/update/bedrooms/UpdateBedroomsJourneyFactory.kt` (content properties omitted):
```kotlin
JourneyBuilder.journey(state) {
    unreachableStepUrl { propertyDetailsRoute }
    step(journey.bedrooms) {
        routeSegment(BedroomsStep.ROUTE_SEGMENT)
        backUrl { propertyDetailsRoute }
        nextStep { journey.completeBedroomsUpdateStep }
        initialStep()
    }
    step(journey.completeBedroomsUpdateStep) {
        parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
        nextUrl { propertyDetailsRoute }
    }
}
```

### Factory Pattern
- Stateful framework components (journey states, tasks, steps and step configs) use `@JourneyFrameworkComponent`, which combines `@PrsdbWebComponent` with prototype scope
- Factories use `ObjectFactory.getObject()` to obtain fresh prototype state/components when building routing maps; journey data is initialised or restored separately
- Factories commonly use `@PrsdbWebService` (e.g. `UpdateBedroomsJourneyFactory`), but this is not universal: `LandlordRegistrationJourneyFactory` itself uses `@JourneyFrameworkComponent`

### Journey State
- Define typed `JourneyState` interfaces and concrete state extending `AbstractJourneyState`; access submitted answers through `step.formModel` / `step.formModelOrNull`
- Store additional state with `delegateProvider.nullableDelegate`, `requiredDelegate` or `requiredImmutableDelegate`, matching the property's nullability and lifecycle. Initialise required values before reading them; immutable delegates are set once
- `FormModel.toPageData()` produces `FormData` (`Map<String, Any?>`) for submitted step data; prefer typed state/form access over ad hoc map getters

### Transaction Metric Tagging
A journey's final commit step must render its submit button via `transactionSubmitButton` (or `transactionWarningButton` for destructive actions) so the completion is counted in the transaction metric — either hardcoded in a commit-only template, or opted into with `"submitButton" to "transactionSubmitButton"` in `withAdditionalContentProperties` where the template is shared with non-commit steps. Tag exactly one button per completed journey; where that is not possible, record the gap in [MetricsReadMe](../../docs/MetricsReadMe.md) rather than risk double-counting.

### Step Config Conventions
- Requestable configs provide `formModelClass`, `getStepSpecificContent(state)`, `chooseTemplate(state)` and `mode(state)`
- `mode(state)` returns an enum value or `null` while unanswered; a step's `outcome` is its mode only when reachable, otherwise `null`
- Customise lifecycle behaviour through `AbstractStepConfig` hooks such as `afterPrimaryValidation(state, bindingResult)`, `afterStepDataIsAdded(state)` and `resolveNextDestination(state, defaultDestination)`

### Navigation Flow
- `nextStep`, `nextUrl` and `nextDestination` choose forward navigation from the current mode; they do not establish reachability
- `parents { ... }` defines reachability using `hasOutcome`, `isComplete`, `AndParents` and `OrParents`. `initialStep()` declares an element with no parents
- Configure back navigation with `backStep` / `backUrl` / `backDestination`, and an unreachable-step destination for requests that fail parentage checks
- `saveProgress()` opts into saving journey state on completion; on a task it applies to the task's savable points, including its exit

### Tasks and Sections
- Implement `Task<State, Dependencies>` (or `TaskWithoutDependencies<State>`) with `taskState` and `makeSubJourney(state) = subJourney(state) { ... }`
- Add tasks with `task(...)`, bind required external state via `withDependencies { ... }`, and use `firstStep` / `exitStep` for entry and completion. The task's next destination is applied at its exit
- Group elements with `section { withHeadingMessageKey(...) ... }` for task-list headings
- Default task status derives from exit reachability and the first step's outcome/reachability

### Check Your Answers
- `CheckYourAnswersJourneyState` creates copied child state via its `ObjectFactory`, tracking child IDs, `checkingAnswersFor` and the return-to-CYA destination
- Factories select a CYA-specific routing map when `checkingAnswersFor` is set. Reuse `checkAnswerStep` / `checkAnswerTask` and `finishCyaStep` rather than building an unrelated update flow
- Build change links with `Destination.VisitableStep(step, state.getCyaJourneyId(step))` to retain child-journey routing; see `shared/states/CheckYourAnswersJourneyState.kt`

### Testing
- Controller tests verify step rendering and submission
- Journey tests verify navigation flow and data persistence
- Use existing journey tests as templates
