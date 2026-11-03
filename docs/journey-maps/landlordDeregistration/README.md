# Delete your landlord account journey map

Generated 2026-11-03T12:28:04.383094700Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Are you sure you want to delete your account? (2 variants)"]
    n1["Why are you deleting your account? (optional)"]
    n2(["Account deleted"])
    n3(["Alexander Smith"])
    journeyStart --> n0
    n0 -->|"Yes, delete"| n1
    n1 --> n2
    n0 -->|"Yes"| n2
    n0 -->|"No"| n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n2 terminal
    class n3 terminal
```

## Pages

### Are you sure you want to delete your account?

`are-you-sure` · AreYouSureStepConfig

[![v1](img/are-you-sure/v1.thumb.jpg)](img/are-you-sure/v1.jpg)

Shown on the default route

[![v2](img/are-you-sure/v2.thumb.jpg)](img/are-you-sure/v2.jpg)

Shown when Starting point: Landlord has no properties


### Why are you deleting your account? (optional)

`reason` · ReasonStepConfig

[![v1](img/reason/v1.thumb.jpg)](img/reason/v1.jpg)

Shown on the default route


### Account deleted

`/landlord/deregister-landlord/confirmation`

[![v1](img/landlord_deregister-landlord_confirmation/v1.thumb.jpg)](img/landlord_deregister-landlord_confirmation/v1.jpg)

Shown on the default route


### Alexander Smith

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown when Starting point: Landlord has no properties; Are you sure you want to delete your account?: No

