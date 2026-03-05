# Local Council User Registration

```mermaid
flowchart TD
    __start__([Start]) --> landingPageStep
    landingPageStep[Landing Page]
    privacyNoticeStep[Privacy Notice]
    nameStep[Name]
    emailStep[Email]
    cyaStep[Check Your Answers]
    landingPageStep --> privacyNoticeStep
    privacyNoticeStep --> nameStep
    nameStep --> emailStep
    emailStep --> cyaStep
    __url__cyaStep[/External URL\]
    cyaStep --> __url__cyaStep
```
