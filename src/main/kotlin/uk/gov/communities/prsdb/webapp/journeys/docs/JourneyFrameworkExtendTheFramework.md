# Extending the Journey Framework
This document covers advanced topics for developers who need to extend or modify the framework.

## Extending the DSL

The DSL is built using Kotlin builder patterns. Key extension points:

- **`JourneyBuilderDsl`** interface: Defines `step()` and `task()` functions
- **`StepInitialiser`**: Configures individual steps with `nextStep()`, `parents()`, `routeSegment()`, etc.
- **`TaskInitialiser`**: Configures tasks within journeys

To add new DSL functions, extend the relevant initialiser classes in `builders/`.

See: https://kotlinlang.org/docs/type-safe-builders.html
