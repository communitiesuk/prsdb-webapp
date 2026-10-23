# Update the governing body journey map

Generated 2026-10-23T09:40:18.143977500Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Gov body member address"]
        n11["What is their contact address?"]
        n12["Select their address"]
        n13["What is their contact address?"]
        n14["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
    end
    subgraph c2["Changing answers"]
        n3["Who do you want to provide details for?"]
        n4["You’ve added 1 person"]
        n5["What is their full name?"]
        n6["What is their date of birth?"]
        n7["What is their contact address?"]
    end
    n0["You’ve added 2 people (3 variants)"]
    n1["Check your answers (4 variants)"]
    n2(["Local Organisation Landlord (4 variants)"])
    n8["Who do you want to provide details for?"]
    n9["What is their full name?"]
    n10["What is their date of birth?"]
    n15{"Remove gov body member"}
    n16(["/landlord/landlord-details#organisation-contacts"])
    n17{"Has any gov body members"}
    n18{"Initialise gov body members for gov body update"}
    journeyStart -->|"Add, change or remove members of your governing body"| n18
    journeyStart -->|"Add, change or remove members of your governing body"| n8
    n0 -->|"Continue"| n1
    n0 -->|"Add another person / Change Person 1 and 2 more"| n8
    n0 -->|"Remove Person 1 / Remove Person 2 / Remove Person 3"| n15
    n15 -->|"Some members"| n0
    n1 -->|"Confirm and submit update"| n2
    n0 -.->|"Add another person / Change Person 1 / Change Person 2"| n3
    n15 -.->|"Some members"| n4
    n3 -.->|"Director / Trustee and 2 more"| n5
    n5 -.->|"Continue"| n6
    n6 -.->|"Continue"| n7
    n8 --> n9
    n9 --> n10
    n10 --> n11
    n11 -->|"Find address"| n12
    n11 -->|"Postcode: ZZ9 9ZZ"| n14
    n12 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n0
    n12 -->|"I want to manually enter my address"| n13
    n12 -->|"Search again"| n11
    n13 --> n0
    n14 -->|"Enter address manually"| n13
    n14 -->|"search again."| n11
    n15 -.->|"No members (not walked)"| n1
    n1 -->|"Complete"| n16
    n17 -->|"No members"| n8
    n17 -->|"Some members"| n0
    n18 -.-> n17
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n2 terminal
    class n3 change
    class n4 change
    class n5 change
    class n6 change
    class n7 change
    class n8 entryPoint
    class n15 decision
    class n16 terminal
    class n17 decision
    class n18 decision
    class n18 entryPoint
```

## Pages

### You’ve added 2 people

`organisation-governing-body-member-list` · OrgGovBodyMemberListStepConfig

[![v1](img/organisation-governing-body-member-list/v1.thumb.jpg)](img/organisation-governing-body-member-list/v1.jpg)

Shown on the default route

[![v2](img/organisation-governing-body-member-list/v2.thumb.jpg)](img/organisation-governing-body-member-list/v2.jpg)

Shown when You’ve added 2 people: Add another person

[![v3](img/organisation-governing-body-member-list/v3.thumb.jpg)](img/organisation-governing-body-member-list/v3.jpg)

Shown when You’ve added 2 people: Remove Person 1


### Check your answers

`governing-body-check-your-answers` · UpdateGoverningBodyCyaStepConfig

[![v1](img/governing-body-check-your-answers/v1.thumb.jpg)](img/governing-body-check-your-answers/v1.jpg)

Shown on the default route

[![v2](img/governing-body-check-your-answers/v2.thumb.jpg)](img/governing-body-check-your-answers/v2.jpg)

Shown when You’ve added 2 people: Add another person

[![v3](img/governing-body-check-your-answers/v3.thumb.jpg)](img/governing-body-check-your-answers/v3.jpg)

Shown when You’ve added 2 people: Remove Person 1

[![v4](img/governing-body-check-your-answers/v4.thumb.jpg)](img/governing-body-check-your-answers/v4.jpg)

Shown when Starting point: Organisation has no governing body members


### Local Organisation Landlord

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown on the default route

[![v2](img/landlord_landlord-details/v2.thumb.jpg)](img/landlord_landlord-details/v2.jpg)

Shown when You’ve added 2 people: Add another person

[![v3](img/landlord_landlord-details/v3.thumb.jpg)](img/landlord_landlord-details/v3.jpg)

Shown when You’ve added 2 people: Remove Person 1

[![v4](img/landlord_landlord-details/v4.thumb.jpg)](img/landlord_landlord-details/v4.jpg)

Shown when Starting point: Organisation has no governing body members


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/change_organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/change_organisation-governing-body-who-to-provide/v1.jpg)

Shown when Check your answers: Change; You’ve added 2 people: Add another person (when changing an answer)


### You’ve added 1 person

`organisation-governing-body-member-list` · OrgGovBodyMemberListStepConfig

[![v1](img/change_organisation-governing-body-member-list/v1.thumb.jpg)](img/change_organisation-governing-body-member-list/v1.jpg)

Shown when Check your answers: Change; You’ve added 2 people: Remove Person 1 (when changing an answer)


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/change_organisation-governing-body-member-name/v1.thumb.jpg)](img/change_organisation-governing-body-member-name/v1.jpg)

Shown when Check your answers: Change; Who do you want to provide details for?: Director (when changing an answer)


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/change_organisation-governing-body-member-dob/v1.thumb.jpg)](img/change_organisation-governing-body-member-dob/v1.jpg)

Shown when Check your answers: Change (when changing an answer)


### What is their contact address?

`organisation-governing-body-member-address/lookup-address` · LookupAddressStepConfig

[![v1](img/change_organisation-governing-body-member-address_lookup-address/v1.thumb.jpg)](img/change_organisation-governing-body-member-address_lookup-address/v1.jpg)

Shown when Check your answers: Change (when changing an answer)


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/organisation-governing-body-who-to-provide/v1.jpg)

Shown when You’ve added 2 people: Add another person


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/organisation-governing-body-member-name/v1.thumb.jpg)](img/organisation-governing-body-member-name/v1.jpg)

Shown when You’ve added 2 people: Add another person


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/organisation-governing-body-member-dob/v1.thumb.jpg)](img/organisation-governing-body-member-dob/v1.jpg)

Shown when You’ve added 2 people: Add another person


### What is their contact address?

`organisation-governing-body-member-address/lookup-address` · LookupAddressStepConfig

[![v1](img/organisation-governing-body-member-address_lookup-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_lookup-address/v1.jpg)

Shown when You’ve added 2 people: Add another person


### Select their address

`organisation-governing-body-member-address/select-address` · SelectAddressStepConfig

[![v1](img/organisation-governing-body-member-address_select-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_select-address/v1.jpg)

Shown when You’ve added 2 people: Add another person


### What is their contact address?

`organisation-governing-body-member-address/manual-address` · ManualAddressStepConfig

[![v1](img/organisation-governing-body-member-address_manual-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_manual-address/v1.jpg)

Shown when You’ve added 2 people: Add another person


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`organisation-governing-body-member-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/organisation-governing-body-member-address_no-address-found/v1.thumb.jpg)](img/organisation-governing-body-member-address_no-address-found/v1.jpg)

Shown when You’ve added 2 people: Add another person; What is their contact address?: Postcode: ZZ9 9ZZ


### /landlord/landlord-details#organisation-contacts

`/landlord/landlord-details#organisation-contacts`

_Not reached by the generator._
