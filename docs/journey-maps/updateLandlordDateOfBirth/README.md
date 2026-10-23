# Update your date of birth journey map

Generated 2026-10-23T09:37:59.906441400Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Update your date of birth"]
    n1(["Alexander Smith"])
    n2{"Check landlord identity verified"}
    journeyStart -->|"Change Date of birth"| n2
    n0 --> n1
    n2 -.->|"Verified / Not verified (not walked)"| n0
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n1 terminal
    class n2 decision
    class n2 entryPoint
```

## Pages

### Update your date of birth

`date-of-birth` · DateOfBirthStepConfig

[![v1](img/date-of-birth/v1.thumb.jpg)](img/date-of-birth/v1.jpg)

Shown on the default route


### Alexander Smith

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown on the default route

