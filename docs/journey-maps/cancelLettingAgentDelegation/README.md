# Remove a property's letting agent journey map

Generated 2026-10-23T09:42:24.573666800Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Are you sure you want to remove letting.agent.one@example.com?"]
    n1(["Letting agent or property manager can no longer provide details"])
    n2(["1 Example Road EG1 1AA"])
    journeyStart -->|"Remove your letting agent or property manager"| n0
    n0 -->|"Yes"| n1
    n0 -->|"No"| n2
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n1 terminal
    class n2 terminal
```

## Pages

### Are you sure you want to remove letting.agent.one@example.com?

`are-you-sure` · AreYouSureStepConfig

[![v1](img/are-you-sure/v1.thumb.jpg)](img/are-you-sure/v1.jpg)

Shown on the default route


### Letting agent or property manager can no longer provide details

`/landlord/property-details/1/remove-letting-agent/confirmation`

[![v1](img/landlord_property-details_1_remove-letting-agent_confirmation/v1.thumb.jpg)](img/landlord_property-details_1_remove-letting-agent_confirmation/v1.jpg)

Shown when Are you sure you want to remove letting.agent.one@example.com?: Yes


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown when Are you sure you want to remove letting.agent.one@example.com?: No

