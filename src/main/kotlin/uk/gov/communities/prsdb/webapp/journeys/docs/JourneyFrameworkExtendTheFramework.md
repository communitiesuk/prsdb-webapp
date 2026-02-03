
## Implementation Notes

This section covers advanced topics for developers who need to extend or modify the framework.

### Adding a New Step

1. Create a step config class extending `AbstractRequestableStepConfig`:

```kotlin
@Component
class MyStepConfig : AbstractRequestableStepConfig<MyMode, MyFormModel, MyJourneyState>() {
    override val formModelClass = MyFormModel::class

    override fun getStepSpecificContent(state: MyJourneyState) = mapOf(
        "someData" to state.someValue
    )

    override fun chooseTemplate(state: MyJourneyState) = "templates/my-step"

    override fun mode(state: MyJourneyState): MyMode? =
        state.myAnswer?.let { MyMode.fromAnswer(it) }
}
```

2. Create a step instance in your journey state class
3. Reference the step in the journey DSL

### Creating a New Task

1. Create a task class extending `Task<TState>`:

```kotlin
class MyTask : Task<MyJourneyState>() {
    override fun makeSubJourney(state: MyJourneyState) = subJourney(state) {
        step(step1) {
            routeSegment("step1")
            initialStep()
            nextStep { step2 }
        }
        step(step2) {
            routeSegment("step2")
            parents { step1.isComplete() }
            nextStep { exitStep }
        }
        exitStep {
            parents { step2.isComplete() }
        }
    }
}
```

2. Create a state interface for the task if needed
3. Use the task in journey definitions with the `task()` DSL function

### Extending the DSL

The DSL is built using Kotlin builder patterns. Key extension points:

- **`JourneyBuilderDsl`** interface: Defines `step()` and `task()` functions
- **`StepInitialiser`**: Configures individual steps with `nextStep()`, `parents()`, `routeSegment()`, etc.
- **`TaskInitialiser`**: Configures tasks within journeys

To add new DSL functions, extend the relevant initialiser classes in `builders/`.

See: https://kotlinlang.org/docs/type-safe-builders.html
