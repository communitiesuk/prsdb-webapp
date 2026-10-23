# Update the organisation type journey map

Generated 2026-10-23T09:38:37.749667400Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Trustee address"]
        n12["What is the lead trustee’s contact address?"]
        n13["Select your address"]
        n14["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
        n15["What is the lead trustee’s contact address?"]
    end
    subgraph c2["Changing answers"]
        n3["You must provide trustee details"]
        n4["What is the lead trustee’s full name?"]
        n5["What is the lead trustee’s date of birth?"]
        n6["What is the lead trustee’s email address?"]
    end
    n0["Select your organisation’s type"]
    n1["Check your answers (2 variants)"]
    n2(["Local Organisation Landlord (4 variants)"])
    n7["You must provide trustee details (4 variants)"]
    n8["What is the lead trustee’s full name?"]
    n9["What is the lead trustee’s date of birth?"]
    n10["What is the lead trustee’s email address?"]
    n11["What is the lead trustee’s phone number?"]
    n16{"Org type update routing"}
    journeyStart -->|"Change Organisation type"| n0
    n0 --> n16
    n16 -->|"Trust unchanged"| n1
    n16 -->|"Adding trust / Removing trust"| n7
    n1 --> n2
    n16 -.->|"Adding trust"| n3
    n3 -.->|"Continue with the change"| n4
    n4 -.->|"Continue"| n5
    n5 -.->|"Continue"| n6
    n7 -->|"Continue with the change"| n8
    n8 --> n9
    n9 --> n10
    n10 --> n11
    n11 --> n12
    n12 -->|"Find address"| n13
    n12 -->|"Postcode: ZZ9 9ZZ"| n14
    n13 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n1
    n13 -->|"I want to manually enter my address"| n15
    n13 -->|"Search again"| n12
    n14 -->|"Enter address manually"| n15
    n14 -->|"search again."| n12
    n15 --> n1
    n7 -->|"Continue with the change"| n1
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n2 terminal
    class n3 change
    class n4 change
    class n5 change
    class n6 change
    class n16 decision
```

## Pages

### Select your organisation’s type

`organisation-type` · OrgTypeStepConfig

[![v1](img/organisation-type/v1.thumb.jpg)](img/organisation-type/v1.jpg)

Shown on the default route


### Check your answers

`organisation-type-check-your-answers` · OrgTypeCyaStepConfig

[![v1](img/organisation-type-check-your-answers/v1.thumb.jpg)](img/organisation-type-check-your-answers/v1.jpg)

Shown on the default route

[![v2](img/organisation-type-check-your-answers/v2.thumb.jpg)](img/organisation-type-check-your-answers/v2.jpg)

Shown when Select your organisation’s type: Trust


### Local Organisation Landlord

`/landlord/landlord-details`

[![v1](img/landlord_landlord-details/v1.thumb.jpg)](img/landlord_landlord-details/v1.jpg)

Shown on the default route

[![v2](img/landlord_landlord-details/v2.thumb.jpg)](img/landlord_landlord-details/v2.jpg)

Shown when Select your organisation’s type: Trust

[![v3](img/landlord_landlord-details/v3.thumb.jpg)](img/landlord_landlord-details/v3.jpg)

Shown when Starting point: Organisation is a trust

[![v4](img/landlord_landlord-details/v4.thumb.jpg)](img/landlord_landlord-details/v4.jpg)

Shown when Starting point: Organisation is a trust; Select your organisation’s type: Trust


### You must provide trustee details

`organisation-type-trust-interruption` · OrgTypeTrustInterruptionStepConfig

[![v1](img/change_organisation-type-trust-interruption/v1.thumb.jpg)](img/change_organisation-type-trust-interruption/v1.jpg)

Shown when Check your answers: Change Organisation type; Select your organisation’s type: Trust (when changing an answer)


### What is the lead trustee’s full name?

`lead-trustee-name` · LeadTrusteeNameStepConfig

[![v1](img/change_lead-trustee-name/v1.thumb.jpg)](img/change_lead-trustee-name/v1.jpg)

Shown when Check your answers: Change Organisation type (when changing an answer)


### What is the lead trustee’s date of birth?

`lead-trustee-dob` · LeadTrusteeDobStepConfig

[![v1](img/change_lead-trustee-dob/v1.thumb.jpg)](img/change_lead-trustee-dob/v1.jpg)

Shown when Check your answers: Change Organisation type (when changing an answer)


### What is the lead trustee’s email address?

`lead-trustee-email` · LeadTrusteeEmailStepConfig

[![v1](img/change_lead-trustee-email/v1.thumb.jpg)](img/change_lead-trustee-email/v1.jpg)

Shown when Check your answers: Change Organisation type (when changing an answer)


### You must provide trustee details

`organisation-type-trust-interruption` · OrgTypeTrustInterruptionStepConfig

[![v1](img/organisation-type-trust-interruption/v1.thumb.jpg)](img/organisation-type-trust-interruption/v1.jpg)

Shown when Select your organisation’s type: Trust

[![v2](img/organisation-type-trust-interruption/v2.thumb.jpg)](img/organisation-type-trust-interruption/v2.jpg)

Shown when Starting point: Organisation is a trust; Select your organisation’s type: Charity

[![v3](img/organisation-type-trust-interruption/v3.thumb.jpg)](img/organisation-type-trust-interruption/v3.jpg)

Shown when Starting point: Organisation is a trust; Select your organisation’s type: None of these

[![v4](img/organisation-type-trust-interruption/v4.thumb.jpg)](img/organisation-type-trust-interruption/v4.jpg)

Shown when Starting point: Organisation is a trust


### What is the lead trustee’s full name?

`lead-trustee-name` · LeadTrusteeNameStepConfig

[![v1](img/lead-trustee-name/v1.thumb.jpg)](img/lead-trustee-name/v1.jpg)

Shown when Select your organisation’s type: Trust


### What is the lead trustee’s date of birth?

`lead-trustee-dob` · LeadTrusteeDobStepConfig

[![v1](img/lead-trustee-dob/v1.thumb.jpg)](img/lead-trustee-dob/v1.jpg)

Shown when Select your organisation’s type: Trust


### What is the lead trustee’s email address?

`lead-trustee-email` · LeadTrusteeEmailStepConfig

[![v1](img/lead-trustee-email/v1.thumb.jpg)](img/lead-trustee-email/v1.jpg)

Shown when Select your organisation’s type: Trust


### What is the lead trustee’s phone number?

`lead-trustee-phone` · LeadTrusteePhoneStepConfig

[![v1](img/lead-trustee-phone/v1.thumb.jpg)](img/lead-trustee-phone/v1.jpg)

Shown when Select your organisation’s type: Trust


### What is the lead trustee’s contact address?

`lead-trustee-address/lookup-address` · LookupAddressStepConfig

[![v1](img/lead-trustee-address_lookup-address/v1.thumb.jpg)](img/lead-trustee-address_lookup-address/v1.jpg)

Shown when Select your organisation’s type: Trust


### Select your address

`lead-trustee-address/select-address` · SelectAddressStepConfig

[![v1](img/lead-trustee-address_select-address/v1.thumb.jpg)](img/lead-trustee-address_select-address/v1.jpg)

Shown when Select your organisation’s type: Trust


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`lead-trustee-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/lead-trustee-address_no-address-found/v1.thumb.jpg)](img/lead-trustee-address_no-address-found/v1.jpg)

Shown when Select your organisation’s type: Trust; What is the lead trustee’s contact address?: Postcode: ZZ9 9ZZ


### What is the lead trustee’s contact address?

`lead-trustee-address/manual-address` · ManualAddressStepConfig

[![v1](img/lead-trustee-address_manual-address/v1.thumb.jpg)](img/lead-trustee-address_manual-address/v1.jpg)

Shown when Select your organisation’s type: Trust; What is the lead trustee’s contact address?: Postcode: ZZ9 9ZZ

