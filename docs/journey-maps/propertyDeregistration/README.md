# Delete a property record journey map

Generated 2026-10-23T09:33:38.248091700Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Deregister 1, Example Road, EG1 1AA"]
    n1["Confirm property deregistration"]
    n2(["Deregistered 1, Example Road, EG1 1AA"])
    n3["Check these actions before you deregister 1, Example Road, EG1 1AA"]
    n4["Deregistering a property when it has joint landlords"]
    n5{"Has pending invitations"}
    n6{"Check can deregister"}
    journeyStart --> n6
    n0 --> n5
    n5 -->|"No"| n1
    n1 --> n2
    n5 -->|"Yes"| n3
    n3 --> n1
    n6 -->|"Single landlord"| n0
    n6 -->|"Has joint landlords"| n4
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n2 terminal
    class n5 decision
    class n6 decision
    class n6 entryPoint
```

## Pages

### Deregister 1, Example Road, EG1 1AA

`info` · DeregisterInfoStepConfig

[![v1](img/info/v1.thumb.jpg)](img/info/v1.jpg)

Shown on the default route


### Confirm property deregistration

`confirm` · ConfirmStepConfig

[![v1](img/confirm/v1.thumb.jpg)](img/confirm/v1.jpg)

Shown on the default route


### Deregistered 1, Example Road, EG1 1AA

`/landlord/deregister-property/1/confirmation`

[![v1](img/landlord_deregister-property_1_confirmation/v1.thumb.jpg)](img/landlord_deregister-property_1_confirmation/v1.jpg)

Shown on the default route


### Check these actions before you deregister 1, Example Road, EG1 1AA

`check-invitations` · CheckPendingInvitationsStepConfig

[![v1](img/check-invitations/v1.thumb.jpg)](img/check-invitations/v1.jpg)

Shown when Starting point: Property has a pending joint landlord invitation


### Deregistering a property when it has joint landlords

`cannot-deregister` · CannotDeregisterStepConfig

[![v1](img/cannot-deregister/v1.thumb.jpg)](img/cannot-deregister/v1.jpg)

Shown when Starting point: Property has joint landlords

