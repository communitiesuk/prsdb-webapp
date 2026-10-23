# Update the correspondence address journey map

Generated 2026-10-23T09:33:49.728385800Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n4["Enter a postal address"]
    end
    n0["Where the council should send post about this property"]
    n1["Select a postal address"]
    n2["Check your answers"]
    n3(["1 Example Road EG1 1AA"])
    n5["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
    n6["Enter a postal address"]
    journeyStart -->|"Change Contact address"| n0
    n0 -->|"Find address"| n1
    n0 -->|"Postcode: ZZ9 9ZZ"| n5
    n1 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n2
    n1 -->|"I want to manually enter my address"| n6
    n1 -->|"Search again"| n0
    n2 --> n3
    n1 -.->|"I want to manually enter my address"| n4
    n4 -.->|"Confirm and continue"| n2
    n5 -->|"Enter address manually"| n6
    n5 -->|"search again."| n0
    n6 --> n2
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

### Where the council should send post about this property

`lookup-address` · LookupAddressStepConfig

[![v1](img/lookup-address/v1.thumb.jpg)](img/lookup-address/v1.jpg)

Shown on the default route


### Select a postal address

`select-address` · SelectAddressStepConfig

[![v1](img/select-address/v1.thumb.jpg)](img/select-address/v1.jpg)

Shown on the default route


### Check your answers

`correspondence-address-check-your-answers` · UpdateCorrespondenceAddressCyaConfig

[![v1](img/correspondence-address-check-your-answers/v1.thumb.jpg)](img/correspondence-address-check-your-answers/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route


### Enter a postal address

`manual-address` · ManualAddressStepConfig

[![v1](img/change_manual-address/v1.thumb.jpg)](img/change_manual-address/v1.jpg)

Shown when Check your answers: Change Postal address; Select a postal address: I want to manually enter my address (when changing an answer)


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`no-address-found` · NoAddressFoundStepConfig

[![v1](img/no-address-found/v1.thumb.jpg)](img/no-address-found/v1.jpg)

Shown when Where the council should send post about this property: Postcode: ZZ9 9ZZ


### Enter a postal address

`manual-address` · ManualAddressStepConfig

[![v1](img/manual-address/v1.thumb.jpg)](img/manual-address/v1.jpg)

Shown when Where the council should send post about this property: Postcode: ZZ9 9ZZ

