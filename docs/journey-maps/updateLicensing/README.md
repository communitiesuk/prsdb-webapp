# Update licensing journey map

Generated 2026-10-23T09:37:56.892263800Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n4["What is your HMO mandatory licence number?"]
        n5["What is your HMO additional licence number?"]
    end
    n0["Update the type of licensing you have for your property"]
    n1["What is your selective licence number?"]
    n2["Check your answers (3 variants)"]
    n3(["1 Example Road EG1 1AA (2 variants)"])
    n6["What is your HMO mandatory licence number?"]
    n7["What is your HMO additional licence number?"]
    n8["Provide licensing later"]
    journeyStart -->|"Change Licensing or Change Licensing type"| n0
    n0 -->|"Selective licence, Continue"| n1
    n0 -->|"HMO mandatory licence, Continue"| n6
    n0 -->|"HMO additional licence, Continue"| n7
    n0 -->|"This property does not need a licence, Continue"| n2
    n1 --> n2
    n2 --> n3
    n0 -.->|"HMO mandatory licence, Continue"| n4
    n0 -.->|"HMO additional licence, Continue"| n5
    n4 -.->|"Continue"| n2
    n5 -.->|"Continue"| n2
    n6 --> n2
    n7 --> n2
    n0 -.->|"Provide later (not walked)"| n8
    n8 -.->|"Complete (not walked)"| n2
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
    class n5 change
    class n8 gap
```

## Pages

### Update the type of licensing you have for your property

`licensing-type` · LicensingTypeStepConfig

[![v1](img/licensing-type/v1.thumb.jpg)](img/licensing-type/v1.jpg)

Shown on the default route


### What is your selective licence number?

`selective-licence` · SelectiveLicenceStepConfig

[![v1](img/selective-licence/v1.thumb.jpg)](img/selective-licence/v1.jpg)

Shown on the default route


### Check your answers

`check-licensing-answers` · UpdateLicensingCyaConfig

[![v1](img/check-licensing-answers/v1.thumb.jpg)](img/check-licensing-answers/v1.jpg)

Shown on the default route

[![v2](img/check-licensing-answers/v2.thumb.jpg)](img/check-licensing-answers/v2.jpg)

Shown when Update the type of licensing you have for your property: This property does not need a licence, Continue

[![v3](img/check-licensing-answers/v3.thumb.jpg)](img/check-licensing-answers/v3.jpg)

Shown when Starting point: Property has a licence; Update the type of licensing you have for your property: This property does not need a licence, Continue


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Update the type of licensing you have for your property: This property does not need a licence, Continue


### What is your HMO mandatory licence number?

`hmo-mandatory-licence` · HmoMandatoryLicenceStepConfig

[![v1](img/change_hmo-mandatory-licence/v1.thumb.jpg)](img/change_hmo-mandatory-licence/v1.jpg)

Shown when Check your answers: Change Licensing type; Update the type of licensing you have for your property: HMO mandatory licence, Continue (when changing an answer)


### What is your HMO additional licence number?

`hmo-additional-licence` · HmoAdditionalLicenceStepConfig

[![v1](img/change_hmo-additional-licence/v1.thumb.jpg)](img/change_hmo-additional-licence/v1.jpg)

Shown when Check your answers: Change Licensing type; Update the type of licensing you have for your property: HMO additional licence, Continue (when changing an answer)


### What is your HMO mandatory licence number?

`hmo-mandatory-licence` · HmoMandatoryLicenceStepConfig

[![v1](img/hmo-mandatory-licence/v1.thumb.jpg)](img/hmo-mandatory-licence/v1.jpg)

Shown when Update the type of licensing you have for your property: HMO mandatory licence, Continue


### What is your HMO additional licence number?

`hmo-additional-licence` · HmoAdditionalLicenceStepConfig

[![v1](img/hmo-additional-licence/v1.thumb.jpg)](img/hmo-additional-licence/v1.jpg)

Shown when Update the type of licensing you have for your property: HMO additional licence, Continue


### Provide licensing later

`provide-licensing-later` · ProvideLicensingLaterStepConfig

_Not reached by the generator._
