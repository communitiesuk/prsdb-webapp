# Update Email

```mermaid
flowchart TD
    __start__([Start]) --> emailStep
    emailStep[Email]
    completeEmailUpdateStep[Complete Email Update]
    emailStep --> completeEmailUpdateStep
    __url__completeEmailUpdateStep[/External URL\]
    completeEmailUpdateStep --> __url__completeEmailUpdateStep
```
