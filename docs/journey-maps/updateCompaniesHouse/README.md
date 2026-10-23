# Update whether the organisation is registered with Companies House journey map

Generated 2026-10-23T09:39:29.588225700Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Org gov body members"]
        subgraph c2["Gov body member address"]
            n12["What is their contact address?"]
            n13["Select their address"]
            n15["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
            n16["What is their contact address?"]
        end
        n9["Who do you want to provide details for?"]
        n10["What is their full name?"]
        n11["What is their date of birth?"]
        n14["You’ve added 1 person (3 variants)"]
        n18{"Has any gov body members"}
        n20{"Remove gov body member"}
    end
    subgraph c3["Changing answers"]
        n4["Your new answer affects other sections of your landlord record"]
        n5["Who do you want to provide details for?"]
        n6["What is their full name?"]
        n7["What is their date of birth?"]
    end
    n0["Is your organisation registered with Companies House?"]
    n1["What is your organisation’s company number?"]
    n2["Check your answers (7 variants)"]
    n3(["Local Organisation Landlord (9 variants)"])
    n8["Your new answer affects other sections of your landlord record (2 variants)"]
    n17{"Org companies house update routing"}
    n19{"Gov body members back routing"}
    journeyStart -->|"Change Registered with Companies House"| n0
    n0 --> n17
    n17 -->|"Unchanged company"| n1
    n17 -->|"Changed to non company / Changed to company"| n8
    n1 --> n2
    n2 --> n3
    n17 -.->|"Changed to non company"| n4
    n4 -.->|"Continue to other sections"| n18
    n18 -.->|"No members"| n5
    n4 -.->|"Go back"| n0
    n5 -.->|"Director / Trustee and 2 more"| n6
    n6 -.->|"Continue"| n7
    n8 -->|"Continue to other sections"| n18
    n18 -->|"No members"| n9
    n8 -->|"Go back"| n0
    n9 --> n10
    n10 --> n11
    n11 --> n12
    n12 -->|"Find address"| n13
    n12 -->|"Postcode: ZZ9 9ZZ"| n15
    n13 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n14
    n13 -->|"I want to manually enter my address"| n16
    n13 -->|"Search again"| n12
    n14 -->|"Continue"| n19
    n19 -->|"Some members"| n2
    n14 -->|"Add another person / Change Person 1 and 2 more"| n9
    n14 -->|"Remove Person 1 / Remove Person 2 / Remove Person 3"| n20
    n20 --> n8
    n15 -->|"Enter address manually"| n16
    n15 -->|"search again."| n12
    n16 --> n14
    n17 -->|"Unchanged non company"| n18
    n18 -->|"Some members"| n14
    n8 -->|"Continue with the change"| n1
    n20 -->|"Some members"| n14
    n20 -->|"No members"| n19
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
    class n6 change
    class n7 change
    class n17 decision
    class n18 decision
    class n19 decision
    class n20 decision
```

## Pages

### Is your organisation registered with Companies House?

`organisation-companies-house` · OrgIsRegisteredCompanyStepConfig

[![v1](img/organisation-companies-house/v1.thumb.jpg)](img/organisation-companies-house/v1.jpg)

Shown on the default route


### What is your organisation’s company number?

`organisation-company-number` · OrgCompanyNumberStepConfig

[![v1](img/organisation-company-number/v1.thumb.jpg)](img/organisation-company-number/v1.jpg)

Shown on the default route


### Check your answers

`check-answers` · CompaniesHouseUpdateCheckAnswersStepConfig

[![v1](img/check-answers/v1.thumb.jpg)](img/check-answers/v1.jpg)

Shown on the default route

[![v2](img/check-answers/v2.thumb.jpg)](img/check-answers/v2.jpg)

Shown when Is your organisation registered with Companies House?: No

[![v3](img/check-answers/v3.thumb.jpg)](img/check-answers/v3.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Trustee

[![v4](img/check-answers/v4.thumb.jpg)](img/check-answers/v4.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Partner

[![v5](img/check-answers/v5.thumb.jpg)](img/check-answers/v5.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Other member of the governing body

[![v6](img/check-answers/v6.thumb.jpg)](img/check-answers/v6.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No

[![v7](img/check-answers/v7.thumb.jpg)](img/check-answers/v7.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No; You’ve added 2 people: Add another person


### Local Organisation Landlord

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown on the default route

[![v2](img/landlord_landlord-details/v2.thumb.jpg)](img/landlord_landlord-details/v2.jpg)

Shown when Is your organisation registered with Companies House?: No

[![v3](img/landlord_landlord-details/v3.thumb.jpg)](img/landlord_landlord-details/v3.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Trustee

[![v4](img/landlord_landlord-details/v4.thumb.jpg)](img/landlord_landlord-details/v4.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Partner

[![v5](img/landlord_landlord-details/v5.thumb.jpg)](img/landlord_landlord-details/v5.jpg)

Shown when Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Other member of the governing body

[![v6](img/landlord_landlord-details/v6.thumb.jpg)](img/landlord_landlord-details/v6.jpg)

Shown when Starting point: Organisation is a trust

[![v7](img/landlord_landlord-details/v7.thumb.jpg)](img/landlord_landlord-details/v7.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No

[![v8](img/landlord_landlord-details/v8.thumb.jpg)](img/landlord_landlord-details/v8.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No; You’ve added 2 people: Add another person

[![v9](img/landlord_landlord-details/v9.thumb.jpg)](img/landlord_landlord-details/v9.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No; You’ve added 2 people: Remove Person 1


### Your new answer affects other sections of your landlord record

`organisation-companies-house-interruption` · OrgCompaniesHouseInterruptionStepConfig

[![v1](img/change_organisation-companies-house-interruption/v1.thumb.jpg)](img/change_organisation-companies-house-interruption/v1.jpg)

Shown when Check your answers: Change Registered with Companies House; Is your organisation registered with Companies House?: No (when changing an answer)


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/change_organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/change_organisation-governing-body-who-to-provide/v1.jpg)

Shown when Check your answers: Change Registered with Companies House; Your new answer affects other sections of your landlord record: Continue to other sections (when changing an answer)


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/change_organisation-governing-body-member-name/v1.thumb.jpg)](img/change_organisation-governing-body-member-name/v1.jpg)

Shown when Check your answers: Change Registered with Companies House; Who do you want to provide details for?: Director (when changing an answer)


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/change_organisation-governing-body-member-dob/v1.thumb.jpg)](img/change_organisation-governing-body-member-dob/v1.jpg)

Shown when Check your answers: Change Registered with Companies House (when changing an answer)


### Your new answer affects other sections of your landlord record

`organisation-companies-house-interruption` · OrgCompaniesHouseInterruptionStepConfig

[![v1](img/organisation-companies-house-interruption/v1.thumb.jpg)](img/organisation-companies-house-interruption/v1.jpg)

Shown when Is your organisation registered with Companies House?: No

[![v2](img/organisation-companies-house-interruption/v2.thumb.jpg)](img/organisation-companies-house-interruption/v2.jpg)

Shown when Starting point: Organisation is a trust


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/organisation-governing-body-who-to-provide/v1.jpg)

Shown when Is your organisation registered with Companies House?: No


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/organisation-governing-body-member-name/v1.thumb.jpg)](img/organisation-governing-body-member-name/v1.jpg)

Shown when Is your organisation registered with Companies House?: No


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/organisation-governing-body-member-dob/v1.thumb.jpg)](img/organisation-governing-body-member-dob/v1.jpg)

Shown when Is your organisation registered with Companies House?: No


### What is their contact address?

`organisation-governing-body-member-address/lookup-address` · LookupAddressStepConfig

[![v1](img/organisation-governing-body-member-address_lookup-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_lookup-address/v1.jpg)

Shown when Is your organisation registered with Companies House?: No


### Select their address

`organisation-governing-body-member-address/select-address` · SelectAddressStepConfig

[![v1](img/organisation-governing-body-member-address_select-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_select-address/v1.jpg)

Shown when Is your organisation registered with Companies House?: No


### You’ve added 1 person

`organisation-governing-body-member-list` · OrgGovBodyMemberListStepConfig

[![v1](img/organisation-governing-body-member-list/v1.thumb.jpg)](img/organisation-governing-body-member-list/v1.jpg)

Shown when Is your organisation registered with Companies House?: No

[![v2](img/organisation-governing-body-member-list/v2.thumb.jpg)](img/organisation-governing-body-member-list/v2.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No

[![v3](img/organisation-governing-body-member-list/v3.thumb.jpg)](img/organisation-governing-body-member-list/v3.jpg)

Shown when Starting point: Organisation is a trust; Is your organisation registered with Companies House?: No; You’ve added 2 people: Add another person


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`organisation-governing-body-member-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/organisation-governing-body-member-address_no-address-found/v1.thumb.jpg)](img/organisation-governing-body-member-address_no-address-found/v1.jpg)

Shown when Is your organisation registered with Companies House?: No; What is their contact address?: Postcode: ZZ9 9ZZ


### What is their contact address?

`organisation-governing-body-member-address/manual-address` · ManualAddressStepConfig

[![v1](img/organisation-governing-body-member-address_manual-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_manual-address/v1.jpg)

Shown when Is your organisation registered with Companies House?: No; What is their contact address?: Postcode: ZZ9 9ZZ

