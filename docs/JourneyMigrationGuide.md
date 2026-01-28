# Journey Migration Guide: Old Framework to New Framework

This guide provides instructions for migrating user journeys from the old journey framework (`src/main/kotlin/forms/journeys/`) to the new
journey framework (`src/main/kotlin/journeys/`).

## Overview

The old framework uses:

- `Journey` base class with `Step` and `Page` classes
- `JourneyDataService` for session-based state management
- Step IDs defined as enums implementing `StepId`
- Factory classes that instantiate journeys with dependencies

The new framework uses:

- `JourneyState` interface with `AbstractJourneyState` base class
- `JourneyStateService` for state management with journey IDs in URLs
- Step configs extending `AbstractRequestableStepConfig`
- DSL-based journey builder for defining step flow
- Feature flags for gradual rollout

## Migration Steps

### 1. Add Feature Flag

Add a new feature flag constant to enable side-by-side operation:

**File: `constants/FeatureFlagNames.kt`**

```kotlin
const val MIGRATE_YOUR_JOURNEY = "migrate-your-journey"

val featureFlagNames = listOf(
    // ...existing flags...
    MIGRATE_YOUR_JOURNEY,
)
```

**Files: `application.yml`, `application-integration.yml`, `src/test/resources/application.yml`**

```yaml
-   name: "migrate-your-journey"
    enabled: false  # Start disabled, enable when ready to test
    expiry-date: "2026-12-31"
```

### 2. Create Step Config Classes

For each step in the old journey, create a step config class in `journeys/yourJourney/steps/`.

**Simple step pattern:**

```kotlin
@JourneyFrameworkComponent
class YourStepConfig : AbstractRequestableStepConfig<Complete, YourFormModel, JourneyState>() {
    override val formModelClass = YourFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = mapOf(
        "title" to "your.title.key",
        "fieldSetHeading" to "your.heading.key",
        // ... other template content
    )

    override fun chooseTemplate(state: JourneyState): String = "forms/yourTemplate"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class YourStep(
    stepConfig: YourStepConfig,
) : RequestableStep<Complete, YourFormModel, JourneyState>(stepConfig)
```

**Notes:**

- Use `JourneyState` as the state type for simple steps that don't need journey-specific data
- Use your custom state interface (e.g., `YourJourneyState`) when steps need access to other steps or journey-specific properties
- The `mode` function determines if the step is complete - return `Complete.COMPLETE` when the form model has valid data

### 3. Create Check Your Answers Step Config

For the CYA step, extend `AbstractCheckYourAnswersStepConfig`:

```kotlin
@JourneyFrameworkComponent
class YourCyaStepConfig(
    // Inject any services needed for registration/submission
    private val yourService: YourService,
) : AbstractCheckYourAnswersStepConfig<YourJourneyState>() {

    override fun chooseTemplate(state: YourJourneyState) = "forms/checkAnswersForm"

    override fun getStepSpecificContent(state: YourJourneyState): Map<String, Any?> = mapOf(
        "title" to "your.title",
        "summaryName" to "your.summaryName",
        "submitButtonText" to "forms.buttons.confirm",
        "summaryListData" to getSummaryList(state),
        "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
    )

    override fun afterStepDataIsAdded(state: YourJourneyState) {
        // Perform the actual registration/submission here
        yourService.register(...)
    }

    private fun getSummaryList(state: YourJourneyState): List<SummaryListRowViewModel> = listOf(
        // IMPORTANT: Use Destination.VisitableStep with childJourneyId for change links
        SummaryListRowViewModel.forCheckYourAnswersPage(
            "your.field.heading",
            state.yourStep.formModel.yourField,
            Destination.VisitableStep(state.yourStep, childJourneyId),  // NOT the route segment string!
        ),
    )
}

@JourneyFrameworkComponent
final class YourCyaStep(
    stepConfig: YourCyaStepConfig,
) : AbstractCheckYourAnswersStep<YourJourneyState>(stepConfig)
```

**Critical: Change Link URLs**

The old framework used:

```kotlin
SummaryListRowViewModel.forCheckYourAnswersPage("heading", value, step.routeSegment)
```

The new framework MUST use:

```kotlin
SummaryListRowViewModel.forCheckYourAnswersPage("heading", value, Destination.VisitableStep(step, childJourneyId))
```

Using the route segment string will cause "Journey already exists" errors when clicking change links.

### 4. Create Journey State and Factory

Create the factory file with the state class and interface:

```kotlin
@PrsdbWebService
class NewYourJourneyFactory(
    private val stateFactory: ObjectFactory<YourJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.firstStep }
            configure {
                withAdditionalContentProperty { "title" to "your.title" }
            }
            step(journey.firstStep) {
                routeSegment("first-step")
                initialStep()
                nextStep { journey.secondStep }
            }
            step(journey.secondStep) {
                routeSegment("second-step")
                parents { journey.firstStep.isComplete() }
                nextStep { journey.cyaStep }
                checkable()  // Mark as checkable for CYA flow
            }
            step(journey.cyaStep) {
                routeSegment("check-answers")
                parents { journey.secondStep.isComplete() }
                nextUrl { "$YOUR_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
            checkYourAnswersJourney()  // Enable CYA child journey support
        }
    }

    fun initializeJourneyState(seed: Any): String =
        stateFactory.getObject().initializeState(seed)
}

@JourneyFrameworkComponent
class YourJourney(
    override val firstStep: FirstStep,
    override val secondStep: SecondStep,
    override val cyaStep: YourCyaStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    YourJourneyState {
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")

    override fun generateJourneyId(seed: Any?): String {
        // Generate a deterministic ID based on seed, or random if no seed
        return super<AbstractJourneyState>.generateJourneyId(
            seed?.let { "Your journey for $it" },
        )
    }
}

interface YourJourneyState : JourneyState, CheckYourAnswersJourneyState {
    val firstStep: FirstStep
    val secondStep: SecondStep
    override val cyaStep: YourCyaStep
}
```

### 5. Create New Controller

Create a new controller with `@AvailableWhenFeatureEnabled`:

```kotlin
@PrsdbController
@RequestMapping(YOUR_ROUTE)
class NewYourController(
    private val journeyFactory: NewYourJourneyFactory,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_YOUR_JOURNEY)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            // Initialize new journey and redirect with journey ID
            val journeyId = journeyFactory.initializeJourneyState(seed)
            val redirectUrl = JourneyStateService.urlWithJourneyState(
                "$YOUR_ROUTE/$stepName",  // IMPORTANT: Use full path
                journeyId,
            )
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_YOUR_JOURNEY)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(seed)
            val redirectUrl = JourneyStateService.urlWithJourneyState(
                "$YOUR_ROUTE/$stepName",  // IMPORTANT: Use full path
                journeyId,
            )
            ModelAndView("redirect:$redirectUrl")
        }
}
```

**Critical: Redirect URLs**

When redirecting with a journey ID, use the FULL path:

```kotlin
JourneyStateService.urlWithJourneyState("$YOUR_ROUTE/$stepName", journeyId)
```

NOT just the step name:

```kotlin
JourneyStateService.urlWithJourneyState(stepName, journeyId)  // WRONG - causes 404
```

### 6. Update Old Controller

Add `@AvailableWhenFeatureDisabled` to all endpoints in the old controller:

```kotlin
@GetMapping("/{stepName}")
@AvailableWhenFeatureDisabled(MIGRATE_YOUR_JOURNEY)
fun getJourneyStep(...) {
    ...
}
```

## Common Pitfalls

### 1. "No such journey with ID: No journey ID provided"

**Cause:** Trying to access journey state before it's initialized, or not including journey ID in redirect URLs.

**Solution:**

- Ensure the entry point initializes the journey and redirects with `?journeyId=...`
- Don't store data in journey state before calling `initializeState()`

### 2. "Journey with ID xxx already exists"

**Cause:** Using a deterministic journey ID and catching `NoSuchJourneyException` to re-initialize.

**Solution:**

- This often indicates the exception is being thrown for a different reason (e.g., CYA child journey)
- Check that CYA change links use `Destination.VisitableStep(step, childJourneyId)` not route segment strings

### 3. 404 Page Not Found after redirect

**Cause:** Redirect URL missing the base route path.

**Solution:** Use full path in redirects:

```kotlin
JourneyStateService.urlWithJourneyState("$YOUR_ROUTE/$stepName", journeyId)
```

### 4. Change links on CYA page start new journey

**Cause:** Using old-style change link URLs with `checkingAnswersFor` parameter instead of child journey ID.

**Solution:** Use `Destination.VisitableStep(step, childJourneyId)` when building summary list rows.

### 5. Data not available in step configs

**Cause:** Trying to access session data that was stored differently in the old framework.

**Solution:**

- Data stored in session services (like invitation tokens) should continue to be accessed from those services
- Don't try to store session data in journey state during initialization - the journey doesn't exist yet
- Access such data directly from the service in step configs

## Testing

1. Start with the feature flag disabled to ensure the old journey still works
2. Enable the flag and test the full journey flow
3. Test the CYA change link flow specifically - click each change link and verify:
    - It navigates to the correct step
    - After saving, it returns to the CYA page
    - The updated value is shown
4. Test edge cases:
    - Refreshing pages mid-journey
    - Using browser back button
    - Direct URL access without journey ID

## File Structure

```
journeys/
  yourJourney/
    NewYourJourneyFactory.kt       # Factory, state class, and state interface
    steps/
      FirstStepConfig.kt           # Step config and step class
      SecondStepConfig.kt
      YourCyaStepConfig.kt         # CYA step config
```
