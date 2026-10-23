# Update whether the organisation is a registered charity journey map

Generated 2026-10-23T09:38:47.133712Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n5["What is your organisation’s charity number?"]
        n6["What is your organisation’s charity number?"]
    end
    n0["Is your organisation a registered charity?"]
    n1["Who is your charity registered with?"]
    n2["What is your organisation’s charity number?"]
    n3["Check your answers (3 variants)"]
    n4(["Local Organisation Landlord (3 variants)"])
    n7["What is your organisation’s charity number?"]
    n8["What is your organisation’s charity number?"]
    journeyStart -->|"Change Registered charity"| n0
    n0 -->|"Yes"| n1
    n0 -->|"No"| n3
    n1 -->|"Charities Commission of England and Wales"| n2
    n1 -->|"Charities Commission of Northern Ireland"| n7
    n1 -->|"Scottish Charity Regulator"| n8
    n1 -->|"None of these"| n3
    n2 --> n3
    n3 --> n4
    n1 -.->|"Charities Commission of Northern Ireland"| n5
    n1 -.->|"Scottish Charity Regulator"| n6
    n5 -.->|"Continue"| n3
    n6 -.->|"Continue"| n3
    n7 --> n3
    n8 --> n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n4 terminal
    class n5 change
    class n6 change
```

## Pages

### Is your organisation a registered charity?

`organisation-charity` · OrgIsRegisteredCharityStepConfig

[![v1](img/organisation-charity/v1.thumb.jpg)](img/organisation-charity/v1.jpg)

Shown on the default route


### Who is your charity registered with?

`organisation-charity-registered-with` · OrgCharityRegisteredWithStepConfig

[![v1](img/organisation-charity-registered-with/v1.thumb.jpg)](img/organisation-charity-registered-with/v1.jpg)

Shown on the default route


### What is your organisation’s charity number?

`organisation-charity-number` · OrgCharityNumberEnglandAndWalesStepConfig

[![v1](img/organisation-charity-number/v1.thumb.jpg)](img/organisation-charity-number/v1.jpg)

Shown on the default route


### Check your answers

`organisation-charity-check-your-answers` · UpdateOrganisationCharityCyaStepConfig

[![v1](img/organisation-charity-check-your-answers/v1.thumb.jpg)](img/organisation-charity-check-your-answers/v1.jpg)

Shown on the default route

[![v2](img/organisation-charity-check-your-answers/v2.thumb.jpg)](img/organisation-charity-check-your-answers/v2.jpg)

Shown when Is your organisation a registered charity?: No

[![v3](img/organisation-charity-check-your-answers/v3.thumb.jpg)](img/organisation-charity-check-your-answers/v3.jpg)

Shown when Who is your charity registered with?: None of these


### Local Organisation Landlord

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown on the default route

[![v2](img/landlord_landlord-details/v2.thumb.jpg)](img/landlord_landlord-details/v2.jpg)

Shown when Is your organisation a registered charity?: No

[![v3](img/landlord_landlord-details/v3.thumb.jpg)](img/landlord_landlord-details/v3.jpg)

Shown when Who is your charity registered with?: None of these


### What is your organisation’s charity number?

`organisation-charity-number-northern-ireland` · OrgCharityNumberNorthernIrelandStepConfig

[![v1](img/change_organisation-charity-number-northern-ireland/v1.thumb.jpg)](img/change_organisation-charity-number-northern-ireland/v1.jpg)

Shown when Check your answers: Change Registered charity; Who is your charity registered with?: Charities Commission of Northern Ireland (when changing an answer)


### What is your organisation’s charity number?

`organisation-charity-number-scotland` · OrgCharityNumberScotlandStepConfig

[![v1](img/change_organisation-charity-number-scotland/v1.thumb.jpg)](img/change_organisation-charity-number-scotland/v1.jpg)

Shown when Check your answers: Change Registered charity; Who is your charity registered with?: Scottish Charity Regulator (when changing an answer)


### What is your organisation’s charity number?

`organisation-charity-number-northern-ireland` · OrgCharityNumberNorthernIrelandStepConfig

[![v1](img/organisation-charity-number-northern-ireland/v1.thumb.jpg)](img/organisation-charity-number-northern-ireland/v1.jpg)

Shown when Who is your charity registered with?: Charities Commission of Northern Ireland


### What is your organisation’s charity number?

`organisation-charity-number-scotland` · OrgCharityNumberScotlandStepConfig

[![v1](img/organisation-charity-number-scotland/v1.thumb.jpg)](img/organisation-charity-number-scotland/v1.jpg)

Shown when Who is your charity registered with?: Scottish Charity Regulator

