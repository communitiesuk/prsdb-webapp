# Update whether the property is occupied journey map

Generated 2026-10-23T09:36:46.692383900Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c0["Changing answers"]
        n3["Are you sure you want to change this?"]
    end
    n0["Update whether your property is occupied by tenants"]
    n1["Check your answers"]
    n2(["1 Example Road EG1 1AA (3 variants)"])
    n4["Are you sure you want to change this?"]
    n5{"Occupancy update routing"}
    journeyStart -->|"Change Is this property occupied by tenants?"| n0
    n0 --> n5
    n5 -->|"No interruption"| n1
    n5 -->|"Show interruption"| n4
    n1 --> n2
    n5 -.->|"Show interruption"| n3
    n3 -.->|"Continue with the change"| n1
    n4 --> n1
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n2 terminal
    class n3 change
    class n5 decision
```

## Pages

### Update whether your property is occupied by tenants

`occupancy` · OccupiedStepConfig

[![v1](img/occupancy/v1.thumb.jpg)](img/occupancy/v1.jpg)

Shown on the default route


### Check your answers

`check-your-answers` · UpdateOccupancyCheckYourAnswersStepConfig

[![v1](img/check-your-answers/v1.thumb.jpg)](img/check-your-answers/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Update whether your property is occupied by tenants: No

[![v3](img/landlord_property-details_1/v3.thumb.jpg)](img/landlord_property-details_1/v3.jpg)

Shown when Starting point: Property is vacant


### Are you sure you want to change this?

`are-you-sure` · OccupancyLettingAgentInterruptionStepConfig

[![v1](img/change_are-you-sure/v1.thumb.jpg)](img/change_are-you-sure/v1.jpg)

Shown when Check your answers: Change Occupied by tenants; Update whether your property is occupied by tenants: No (when changing an answer)


### Are you sure you want to change this?

`are-you-sure` · OccupancyLettingAgentInterruptionStepConfig

[![v1](img/are-you-sure/v1.thumb.jpg)](img/are-you-sure/v1.jpg)

Shown when Update whether your property is occupied by tenants: No

