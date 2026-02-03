# Journey Framework

The Journey Framework is designed to make creating complex multi-page form journeys quick and easy without compromising on flexibility.

## Overview

The framework aims to:
- Commonise behaviours of multi-page form journeys without making it difficult to implement custom requirements
- Allow pages and groups of pages to be reused across different journeys
- Separate the structure of journeys from the content to make both easier to maintain and update


## Quick Start

### Creating a New Journey Checklist

1. Create step classes (define pages, validation, and data storage)
2. Define a state interface for your journey
3. Implement a journey state class
4. Define the journey structure using the DSL
5. Add controller methods for GET and POST requests
6. Write tests

### Minimal Journey Example

```kotlin
val simpleJourney = journey(state) {
    step(journey.firstStep) {
        nextStep { journey.secondStep }
        routeSegment("step-1")
        initialStep()
    }
    step(journey.secondStep) {
        nextUrl { "/home" }
        routeSegment("step-2")
        parents { journey.firstStep.isComplete() }
    }
}
```
