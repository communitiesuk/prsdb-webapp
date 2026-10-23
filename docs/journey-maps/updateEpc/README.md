# Update the energy performance certificate journey map

Generated 2026-10-23T09:35:57.904050200Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n5["Is an EPC required to let this property?"]
        n6["Your property is missing an EPC"]
        n7["Why does this property not need an EPC to be let?"]
        n13["This property’s EPC has expired"]
        n16["This property does not meet energy efficiency requirements for letting"]
    end
    n0["Do you have an EPC for this property?"]
    n1["Find your EPC"]
    n2["Confirm your EPC"]
    n3["Energy performance certificate (EPC) (8 variants)"]
    n4(["1 Example Road EG1 1AA (12 variants)"])
    n8["Is an EPC required to let this property?"]
    n9["Your property is missing an EPC (2 variants)"]
    n10["We could not find your EPC"]
    n11["There’s a newer EPC for this property"]
    n12["Confirm when the current tenancy began"]
    n14["You need a registered energy efficiency exemption to let this property"]
    n15["Which energy efficiency exemption did you register for this property?"]
    n17["Why does this property not need an EPC to be let?"]
    n18["This property’s EPC has expired (2 variants)"]
    n19["This property does not meet energy efficiency requirements for letting (2 variants)"]
    n20["Check this EPC"]
    n21{"Epc age check"}
    n22{"Epc energy rating check"}
    n23{"Property occupied check"}
    n24["Provide epc later"]
    n25(["/landlord/property-details/1#compliance-information"])
    n26{"Epc lookup by uprn"}
    n27{"Start epc"}
    journeyStart -->|"Change"| n27
    journeyStart -->|"Change"| n20
    journeyStart -->|"Change"| n0
    n0 -->|"Yes, Continue"| n1
    n0 -->|"No (for example, the property does not require an EPC to be let), Continue"| n8
    n1 -->|"Search"| n2
    n1 -->|"Search"| n10
    n1 -->|"Search"| n11
    n2 -->|"Yes"| n21
    n21 -->|"Epc 10 years or newer"| n22
    n22 -->|"Epc meets energy requirements"| n3
    n2 -->|"No, search again"| n1
    n3 -->|"Confirm and submit update"| n4
    n0 -.->|"No (for example, the property does not require an EPC to be let), Continue"| n5
    n5 -.->|"Yes"| n6
    n5 -.->|"No"| n7
    n6 -.->|"Continue anyway"| n3
    n7 -.->|"It has protected architectural or historical merit / It’s smaller than 50 metres squared and 3 more"| n3
    n8 -->|"Yes"| n9
    n8 -->|"No"| n17
    n9 --> n3
    n10 -->|"I do not have an EPC for this property"| n8
    n10 -->|"search again"| n1
    n11 -->|"Continue with most recent EPC"| n21
    n11 -->|"Search by certificate number again"| n1
    n21 -->|"Epc older than 10 years"| n23
    n23 -->|"Yes"| n12
    n12 -->|"Yes"| n22
    n12 -->|"No"| n18
    n12 -.->|"No"| n13
    n13 -.->|"Continue anyway"| n3
    n22 -->|"Epc low energy rating"| n14
    n14 -->|"Yes"| n15
    n14 -->|"No"| n19
    n15 --> n3
    n14 -.->|"No"| n16
    n16 -.->|"Continue anyway"| n3
    n17 --> n3
    n18 --> n3
    n19 --> n3
    n20 -->|"Yes"| n21
    n20 -->|"No (use a different EPC or the property does not need one)"| n0
    n23 -->|"No"| n18
    n0 -.->|"Provide later (not walked)"| n24
    n24 -.->|"Complete (not walked)"| n3
    n3 -->|"Complete"| n25
    n26 -->|"Epc found"| n20
    n26 -->|"Not found"| n0
    n27 -.-> n26
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
    class n7 change
    class n13 change
    class n16 change
    class n20 entryPoint
    class n21 decision
    class n22 decision
    class n23 decision
    class n24 gap
    class n25 terminal
    class n26 decision
    class n27 decision
    class n27 entryPoint
```

## Pages

### Do you have an EPC for this property?

`has-epc` · HasEpcStepConfig

[![v1](img/has-epc/v1.thumb.jpg)](img/has-epc/v1.jpg)

Shown on the default route


### Find your EPC

`find-your-epc` · FindYourEpcStepConfig

[![v1](img/find-your-epc/v1.thumb.jpg)](img/find-your-epc/v1.jpg)

Shown on the default route


### Confirm your EPC

`confirm-epc-details` · ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig

[![v1](img/confirm-epc-details/v1.thumb.jpg)](img/confirm-epc-details/v1.jpg)

Shown on the default route


### Energy performance certificate (EPC)

`check-epc-answers` · UpdateCheckEpcAnswersStepConfig

[![v1](img/check-epc-answers/v1.thumb.jpg)](img/check-epc-answers/v1.jpg)

Shown on the default route

[![v2](img/check-epc-answers/v2.thumb.jpg)](img/check-epc-answers/v2.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue

[![v3](img/check-epc-answers/v3.thumb.jpg)](img/check-epc-answers/v3.jpg)

Shown when EPC register search by certificate number: Expired certificate found

[![v4](img/check-epc-answers/v4.thumb.jpg)](img/check-epc-answers/v4.jpg)

Shown when EPC register search by certificate number: Low rated certificate found

[![v5](img/check-epc-answers/v5.thumb.jpg)](img/check-epc-answers/v5.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue; Is an EPC required to let this property?: No

[![v6](img/check-epc-answers/v6.thumb.jpg)](img/check-epc-answers/v6.jpg)

Shown when EPC register search by certificate number: Expired certificate found; Confirm when the current tenancy began: No

[![v7](img/check-epc-answers/v7.thumb.jpg)](img/check-epc-answers/v7.jpg)

Shown when EPC register search by certificate number: Low rated certificate found; You need a registered energy efficiency exemption to let this property: No

[![v8](img/check-epc-answers/v8.thumb.jpg)](img/check-epc-answers/v8.jpg)

Shown when Starting point: Property is vacant; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue

[![v3](img/landlord_property-details_1/v3.thumb.jpg)](img/landlord_property-details_1/v3.jpg)

Shown when EPC register search by certificate number: Expired certificate found

[![v4](img/landlord_property-details_1/v4.thumb.jpg)](img/landlord_property-details_1/v4.jpg)

Shown when EPC register search by certificate number: Low rated certificate found

[![v5](img/landlord_property-details_1/v5.thumb.jpg)](img/landlord_property-details_1/v5.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue; Is an EPC required to let this property?: No

[![v6](img/landlord_property-details_1/v6.thumb.jpg)](img/landlord_property-details_1/v6.jpg)

Shown when EPC register search by certificate number: Expired certificate found; Confirm when the current tenancy began: No

[![v7](img/landlord_property-details_1/v7.thumb.jpg)](img/landlord_property-details_1/v7.jpg)

Shown when EPC register search by certificate number: Low rated certificate found; You need a registered energy efficiency exemption to let this property: No

[![v8](img/landlord_property-details_1/v8.thumb.jpg)](img/landlord_property-details_1/v8.jpg)

Shown when Starting point: Property is vacant

[![v9](img/landlord_property-details_1/v9.thumb.jpg)](img/landlord_property-details_1/v9.jpg)

Shown when Starting point: Property is vacant; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue

[![v10](img/landlord_property-details_1/v10.thumb.jpg)](img/landlord_property-details_1/v10.jpg)

Shown when Starting point: Property is vacant; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue; Is an EPC required to let this property?: No

[![v11](img/landlord_property-details_1/v11.thumb.jpg)](img/landlord_property-details_1/v11.jpg)

Shown when Starting point: Property is vacant; EPC register search by address: Expired EPC found

[![v12](img/landlord_property-details_1/v12.thumb.jpg)](img/landlord_property-details_1/v12.jpg)

Shown when Starting point: Property is vacant; EPC register search by address: Low rated EPC found


### Is an EPC required to let this property?

`is-epc-required` · IsEpcRequiredStepConfig

[![v1](img/change_is-epc-required/v1.thumb.jpg)](img/change_is-epc-required/v1.jpg)

Shown when Energy performance certificate (EPC): Change; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue (when changing an answer)


### Your property is missing an EPC

`epc-missing` · EpcMissingStepConfig

[![v1](img/change_epc-missing/v1.thumb.jpg)](img/change_epc-missing/v1.jpg)

Shown when Energy performance certificate (EPC): Change; Is an EPC required to let this property?: Yes (when changing an answer)


### Why does this property not need an EPC to be let?

`epc-exemption` · EpcExemptionStepConfig

[![v1](img/change_epc-exemption/v1.thumb.jpg)](img/change_epc-exemption/v1.jpg)

Shown when Energy performance certificate (EPC): Change; Is an EPC required to let this property?: No (when changing an answer)


### Is an EPC required to let this property?

`is-epc-required` · IsEpcRequiredStepConfig

[![v1](img/is-epc-required/v1.thumb.jpg)](img/is-epc-required/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue


### Your property is missing an EPC

`epc-missing` · EpcMissingStepConfig

[![v1](img/epc-missing/v1.thumb.jpg)](img/epc-missing/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue

[![v2](img/epc-missing/v2.thumb.jpg)](img/epc-missing/v2.jpg)

Shown when Starting point: Property is vacant; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue


### We could not find your EPC

`epc-not-found` · EpcNotFoundStepConfig

[![v1](img/epc-not-found/v1.thumb.jpg)](img/epc-not-found/v1.jpg)

Shown when EPC register search by certificate number: Certificate not found


### There’s a newer EPC for this property

`epc-superseded` · EpcSuperseededStepConfig

[![v1](img/epc-superseded/v1.thumb.jpg)](img/epc-superseded/v1.jpg)

Shown when EPC register search by certificate number: Superseded certificate found


### Confirm when the current tenancy began

`epc-in-date-at-start-of-tenancy-check` · EpcInDateAtStartOfTenancyCheckStepConfig

[![v1](img/epc-in-date-at-start-of-tenancy-check/v1.thumb.jpg)](img/epc-in-date-at-start-of-tenancy-check/v1.jpg)

Shown when EPC register search by certificate number: Expired certificate found


### This property’s EPC has expired

`epc-expired` · EpcExpiredStepConfig

[![v1](img/change_epc-expired/v1.thumb.jpg)](img/change_epc-expired/v1.jpg)

Shown when EPC register search by certificate number: Expired certificate found; Energy performance certificate (EPC): Change Was the EPC still in date when the current tenancy began?; Confirm when the current tenancy began: No (when changing an answer)


### You need a registered energy efficiency exemption to let this property

`has-mees-exemption` · HasMeesExemptionStepConfig

[![v1](img/has-mees-exemption/v1.thumb.jpg)](img/has-mees-exemption/v1.jpg)

Shown when EPC register search by certificate number: Low rated certificate found


### Which energy efficiency exemption did you register for this property?

`mees-exemption` · MeesExemptionStepConfig

[![v1](img/mees-exemption/v1.thumb.jpg)](img/mees-exemption/v1.jpg)

Shown when EPC register search by certificate number: Low rated certificate found


### This property does not meet energy efficiency requirements for letting

`low-energy-rating` · LowEnergyRatingStepConfig

[![v1](img/change_low-energy-rating/v1.thumb.jpg)](img/change_low-energy-rating/v1.jpg)

Shown when EPC register search by certificate number: Low rated certificate found; Energy performance certificate (EPC): Change Do you have a registered energy efficiency exemption for this property?; You need a registered energy efficiency exemption to let this property: No (when changing an answer)


### Why does this property not need an EPC to be let?

`epc-exemption` · EpcExemptionStepConfig

[![v1](img/epc-exemption/v1.thumb.jpg)](img/epc-exemption/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Continue; Is an EPC required to let this property?: No


### This property’s EPC has expired

`epc-expired` · EpcExpiredStepConfig

[![v1](img/epc-expired/v1.thumb.jpg)](img/epc-expired/v1.jpg)

Shown when EPC register search by certificate number: Expired certificate found; Confirm when the current tenancy began: No

[![v2](img/epc-expired/v2.thumb.jpg)](img/epc-expired/v2.jpg)

Shown when Starting point: Property is vacant; EPC register search by address: Expired EPC found


### This property does not meet energy efficiency requirements for letting

`low-energy-rating` · LowEnergyRatingStepConfig

[![v1](img/low-energy-rating/v1.thumb.jpg)](img/low-energy-rating/v1.jpg)

Shown when EPC register search by certificate number: Low rated certificate found; You need a registered energy efficiency exemption to let this property: No

[![v2](img/low-energy-rating/v2.thumb.jpg)](img/low-energy-rating/v2.jpg)

Shown when Starting point: Property is vacant; EPC register search by address: Low rated EPC found; You need a registered energy efficiency exemption to let this property: No


### Check this EPC

`check-epc-details` · ConfirmEpcRetrievedByUprnStepConfig

[![v1](img/check-epc-details/v1.thumb.jpg)](img/check-epc-details/v1.jpg)

Shown when EPC register search by address: EPC found


### Provide epc later

`provide-epc-later` · ProvideEpcLaterStepConfig

_Not reached by the generator._

### /landlord/property-details/1#compliance-information

`/landlord/property-details/1#compliance-information`

_Not reached by the generator._
