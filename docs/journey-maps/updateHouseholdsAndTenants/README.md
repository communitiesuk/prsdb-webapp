# Update households and tenants journey map

Generated 2026-10-23T09:36:32.466466800Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Households in your property"]
    n1["Update how many people live in your property"]
    n2["Check your answers"]
    n3(["1 Example Road EG1 1AA"])
    n4["Provide tenancy details later"]
    journeyStart -->|"Change Number of households"| n0
    n0 --> n1
    n1 --> n2
    n2 --> n3
    n0 -.->|"Provide this later (not walked)"| n4
    n4 -.->|"Complete (not walked)"| n2
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n3 terminal
    class n4 gap
```

## Pages

### Households in your property

`number-of-households` · HouseholdStepConfig

[![v1](img/number-of-households/v1.thumb.jpg)](img/number-of-households/v1.jpg)

Shown on the default route


### Update how many people live in your property

`number-of-people` · TenantsStepConfig

[![v1](img/number-of-people/v1.thumb.jpg)](img/number-of-people/v1.jpg)

Shown on the default route


### Check your answers

`households-tenants-check-your-answers` · UpdateHouseholdsAndTenantsCyaConfig

[![v1](img/households-tenants-check-your-answers/v1.thumb.jpg)](img/households-tenants-check-your-answers/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route


### Provide tenancy details later

`provide-tenancy-details-later` · ProvideTenancyDetailsLaterStepConfig

_Not reached by the generator._
