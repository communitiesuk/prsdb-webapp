# Accept an invitation to act as a letting agent journey map

Generated 2026-11-03T12:37:34.521369600Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Set your password for this property"]
    n1["Property password created"]
    n2(["1 Example Road EG1 1AA"])
    n3["Enter the password for this property"]
    n4{"Has password"}
    n5{"Start"}
    journeyStart -->|"Link in the invitation email"| n5
    n0 --> n1
    n1 --> n2
    n3 --> n2
    n4 -->|"Has password"| n3
    n4 -->|"No password"| n0
    n5 -.-> n4
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n2 terminal
    class n4 decision
    class n5 decision
    class n5 entryPoint
```

## Pages

### Set your password for this property

`set-password` · SetPasswordStepConfig

[![v1](img/set-password/v1.thumb.jpg)](img/set-password/v1.jpg)

Shown on the default route


### Property password created

`confirmation` · ConfirmationStepConfig

[![v1](img/confirmation/v1.thumb.jpg)](img/confirmation/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/letting-agent/property-details/3334abcd-5678-abcd-1234-567abcd1111a`

[![v1](img/landlord_letting-agent_property-details_3334abcd-5678-abcd-1234-567abcd1111a/v1.thumb.jpg)](img/landlord_letting-agent_property-details_3334abcd-5678-abcd-1234-567abcd1111a/v1.jpg)

Shown on the default route


### Enter the password for this property

`enter-password` · EnterPasswordStepConfig

[![v1](img/enter-password/v1.thumb.jpg)](img/enter-password/v1.jpg)

Shown when Starting point: Letting agent already has a password

