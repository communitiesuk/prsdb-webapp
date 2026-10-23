# Register as a local council user journey map

Generated 2026-10-23T09:42:45.589621900Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Read and agree to the privacy notice"]
    n1["Registering as a local council user"]
    n2["What is your full name?"]
    n3["What is your work email address?"]
    n4["Check your answers"]
    n5(["You’ve registered as a BRISTOL CITY COUNCIL user"])
    journeyStart -->|"Link in the invitation email"| n0
    n0 --> n1
    n1 --> n2
    n2 --> n3
    n3 --> n4
    n4 --> n5
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n5 terminal
```

## Pages

### Read and agree to the privacy notice

`privacy-notice` · PrivacyNoticeStepConfig

[![v1](img/privacy-notice/v1.thumb.jpg)](img/privacy-notice/v1.jpg)

Shown on the default route


### Registering as a local council user

`landing-page` · LandingPageStepConfig

[![v1](img/landing-page/v1.thumb.jpg)](img/landing-page/v1.jpg)

Shown on the default route


### What is your full name?

`name` · NameStepConfig

[![v1](img/name/v1.thumb.jpg)](img/name/v1.jpg)

Shown on the default route


### What is your work email address?

`email` · EmailStepConfig

[![v1](img/email/v1.thumb.jpg)](img/email/v1.jpg)

Shown on the default route


### Check your answers

`check-answers` · LocalCouncilUserCyaStepConfig

[![v1](img/check-answers/v1.thumb.jpg)](img/check-answers/v1.jpg)

Shown on the default route


### You’ve registered as a BRISTOL CITY COUNCIL user

`/local-council/register-local-council-user/confirmation`

[![v1](img/local-council_register-local-council-user_confirmation/v1.thumb.jpg)](img/local-council_register-local-council-user_confirmation/v1.jpg)

Shown on the default route

