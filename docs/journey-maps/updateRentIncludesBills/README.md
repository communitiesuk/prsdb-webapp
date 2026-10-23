# Update whether rent includes bills journey map

Generated 2026-10-23T09:37:10.029076500Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Update whether the rent includes bills"]
    n1["Update which of these you include in the rent"]
    n2["Check your answers (2 variants)"]
    n3(["1 Example Road EG1 1AA (2 variants)"])
    journeyStart -->|"Change Rent includes bills"| n0
    n0 -->|"Yes"| n1
    n0 -->|"No"| n2
    n1 --> n2
    n2 --> n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n3 terminal
```

## Pages

### Update whether the rent includes bills

`rent-includes-bills` · RentIncludesBillsStepConfig

[![v1](img/rent-includes-bills/v1.thumb.jpg)](img/rent-includes-bills/v1.jpg)

Shown on the default route


### Update which of these you include in the rent

`bills-included` · BillsIncludedStepConfig

[![v1](img/bills-included/v1.thumb.jpg)](img/bills-included/v1.jpg)

Shown on the default route


### Check your answers

`rent-includes-bills-check-your-answers` · UpdateRentIncludesBillsCyaConfig

[![v1](img/rent-includes-bills-check-your-answers/v1.thumb.jpg)](img/rent-includes-bills-check-your-answers/v1.jpg)

Shown on the default route

[![v2](img/rent-includes-bills-check-your-answers/v2.thumb.jpg)](img/rent-includes-bills-check-your-answers/v2.jpg)

Shown when Update whether the rent includes bills: No


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Update whether the rent includes bills: No

