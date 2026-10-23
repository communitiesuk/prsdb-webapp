# Update the rent frequency and amount journey map

Generated 2026-10-23T09:36:57.440805300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n4["Update your 4-weekly rent (2 variants)"]
    end
    n0["Update when you charge rent"]
    n1["Update your monthly rent (4 variants)"]
    n2["Check your answers"]
    n3(["1 Example Road EG1 1AA"])
    journeyStart -->|"Change When rent is paid"| n0
    n0 --> n1
    n1 --> n2
    n2 --> n3
    n0 -.->|"4 weekly / Weekly"| n4
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n3 terminal
    class n4 change
```

## Pages

### Update when you charge rent

`rent-frequency` · RentFrequencyStepConfig

[![v1](img/rent-frequency/v1.thumb.jpg)](img/rent-frequency/v1.jpg)

Shown on the default route


### Update your monthly rent

`rent-amount` · RentAmountStepConfig

[![v1](img/rent-amount/v1.thumb.jpg)](img/rent-amount/v1.jpg)

Shown on the default route

[![v2](img/rent-amount/v2.thumb.jpg)](img/rent-amount/v2.jpg)

Shown when Check your answers: Change When rent is paid; Update when you charge rent: Other (when changing an answer)

[![v3](img/rent-amount/v3.thumb.jpg)](img/rent-amount/v3.jpg)

Shown when Update when you charge rent: 4 weekly

[![v4](img/rent-amount/v4.thumb.jpg)](img/rent-amount/v4.jpg)

Shown when Update when you charge rent: Weekly


### Check your answers

`rent-frequency-and-amount-check-your-answers` · UpdateRentFrequencyAndAmountCyaConfig

[![v1](img/rent-frequency-and-amount-check-your-answers/v1.thumb.jpg)](img/rent-frequency-and-amount-check-your-answers/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route


### Update your 4-weekly rent

`rent-amount` · RentAmountStepConfig

[![v1](img/change_rent-amount/v1.thumb.jpg)](img/change_rent-amount/v1.jpg)

Shown when Check your answers: Change When rent is paid; Update when you charge rent: 4 weekly (when changing an answer)

[![v2](img/change_rent-amount/v2.thumb.jpg)](img/change_rent-amount/v2.jpg)

Shown when Check your answers: Change When rent is paid; Update when you charge rent: Weekly (when changing an answer)

