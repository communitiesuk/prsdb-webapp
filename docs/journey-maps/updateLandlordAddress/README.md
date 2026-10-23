# Update your address journey map

Generated 2026-10-23T09:38:09.220738Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["What is your contact address?"]
    n1["Select your address"]
    n2(["Alexander Smith"])
    n3["What is your contact address?"]
    n4["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
    journeyStart -->|"Change Contact address"| n0
    n0 -->|"Find address"| n1
    n0 -->|"Postcode: ZZ9 9ZZ"| n4
    n1 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n2
    n1 -->|"I want to manually enter my address"| n3
    n1 -->|"Search again"| n0
    n3 --> n2
    n4 -->|"Enter address manually"| n3
    n4 -->|"search again."| n0
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n2 terminal
```

## Pages

### What is your contact address?

`lookup-address` · LookupAddressStepConfig

[![v1](img/lookup-address/v1.thumb.jpg)](img/lookup-address/v1.jpg)

Shown on the default route


### Select your address

`select-address` · SelectAddressStepConfig

[![v1](img/select-address/v1.thumb.jpg)](img/select-address/v1.jpg)

Shown on the default route


### Alexander Smith

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown when Select your address: Slate House, PRSDB Square, AA3 1AB


### What is your contact address?

`manual-address` · ManualAddressStepConfig

[![v1](img/manual-address/v1.thumb.jpg)](img/manual-address/v1.jpg)

Shown on the default route


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`no-address-found` · NoAddressFoundStepConfig

[![v1](img/no-address-found/v1.thumb.jpg)](img/no-address-found/v1.jpg)

Shown when What is your contact address?: Postcode: ZZ9 9ZZ

