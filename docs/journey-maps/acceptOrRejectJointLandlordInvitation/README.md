# Accept or reject a joint landlord invitation journey map

Generated 2026-10-23T09:42:19.934191300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c0["Landlord registration"]
        subgraph c1["Identity"]
            n5["You have not proven your identity using GOV.UK One Login"]
            n6["What is your full name?"]
            n7["What is your date of birth?"]
            n22["Confirm your name and date of birth"]
            n71{"Identity verifying"}
        end
        subgraph c2["Org landlord registration"]
            subgraph c3["Org address"]
                n24["What is your organisation’s address?"]
                n25["Select your organisation’s address"]
                n47["No matching address in England or Wales found for ZZ9 9ZZ and 1"]
                n48["What is your organisation’s address?"]
            end
            subgraph c4["Lead trustee"]
                subgraph c5["Trustee address"]
                    n53["What is the lead trustee’s contact address?"]
                    n54["Select your address"]
                    n65["No matching address in England or Wales found for ZZ9 9ZZ and 1"]
                    n66["What is the lead trustee’s contact address?"]
                end
                n49["What is the lead trustee’s full name?"]
                n50["What is the lead trustee’s date of birth?"]
                n51["What is the lead trustee’s email address?"]
                n52["What is the lead trustee’s phone number?"]
            end
            subgraph c6["Org charity"]
                n29["Is your organisation a registered charity?"]
                n30["Who is your charity registered with?"]
                n31["What is your organisation’s charity number?"]
                n55["What is your organisation’s charity number?"]
                n56["What is your organisation’s charity number?"]
            end
            subgraph c7["Org companies house"]
                n32["Is your organisation registered with Companies House?"]
                n33["What is your organisation’s company number?"]
            end
            subgraph c8["Org gov body"]
                subgraph c9["Org gov body members"]
                    subgraph c10["Gov body member address"]
                        n62["What is their contact address?"]
                        n63["Select their address"]
                        n67["No matching address in England or Wales found for ZZ9 9ZZ and 1"]
                        n68["What is their contact address?"]
                    end
                    n59["Who do you want to provide details for?"]
                    n60["What is their full name?"]
                    n61["What is their date of birth?"]
                    n64["You’ve added 1 person"]
                    n74{"Has any gov body members"}
                    n75{"Has any gov body members"}
                    n77{"Remove gov body member"}
                end
                n57["Providing details about your organisation’s governing body"]
                n58["You need to provide more information to register"]
                n76{"Gov body members back routing"}
            end
            n23["What is the name of your organisation?"]
            n26["What is your organisation’s email address?"]
            n27["What is your organisation’s phone number?"]
            n28["Select your organisation’s type"]
            n34["Who is the main contact for your organisation?"]
        end
        subgraph c11["Individual landlord location"]
            subgraph c12["Landlord address"]
                n13["What is your contact address?"]
                n14["Select your address"]
                n45["No matching address in England or Wales found for ZZ9 9ZZ and 1"]
                n46["What is your contact address?"]
            end
            n11["Do you live in England or Wales?"]
            n12["This private beta is only available to landlords who live in England or Wales"]
        end
        n4["Your data privacy"]
        n8["What is your email address?"]
        n9["What is your phone number?"]
        n10["Are you registering as an individual or an organisation?"]
        n15["Check your answers (10 variants)"]
    end
    subgraph c13["Org companies house change gov body"]
    end
    subgraph c14["Changing answers"]
        n16["What is the name of your organisation?"]
        n17["What is your organisation’s address?"]
        n18["Select your organisation’s address"]
        n19["What is your organisation’s email address?"]
        n20["What is your organisation’s address?"]
        n21["What is your contact address?"]
        n35["You must provide trustee details"]
        n36["What is the lead trustee’s full name?"]
        n37["What is the lead trustee’s date of birth?"]
        n38["What is the lead trustee’s email address?"]
        n39["What is your organisation’s charity number?"]
        n40["What is your organisation’s charity number?"]
        n41["Your new answer affects other sections of your landlord record"]
        n42["Who do you want to provide details for?"]
        n43["What is their full name?"]
        n44["What is their date of birth?"]
    end
    n0["Tell us if you’re a joint landlord for a property"]
    n1(["Invite rejected"])
    n2["Confirm that you’re a joint landlord for this property (2 variants)"]
    n3(["Registered as a joint landlord"])
    n69["There was a problem with this invitation link"]
    n70{"Check user role"}
    n72{"Org type update routing"}
    n73{"Org companies house update routing"}
    n78{"Validate token"}
    journeyStart -->|"Link in the invitation email"| n78
    n0 -->|"Yes (you’ll need to sign in with GOV.UK One Login)"| n70
    n70 -->|"User is already registered as landlord"| n2
    n0 -->|"No"| n1
    n2 --> n3
    n70 -->|"User not registered as landlord"| n4
    n4 --> n71
    n71 -->|"Not verified"| n5
    n71 -->|"Verified"| n22
    n5 --> n6
    n6 --> n7
    n7 --> n8
    n8 --> n9
    n9 --> n10
    n10 -->|"An individual"| n11
    n10 -->|"An organisation"| n23
    n11 -->|"Yes"| n13
    n11 -->|"No"| n12
    n13 -->|"Find address"| n14
    n13 -->|"Postcode: ZZ9 9ZZ"| n45
    n14 -->|"1 Fictional Road / 2 Fake Way"| n15
    n14 -->|"I want to manually enter my address"| n46
    n14 -->|"Search again"| n13
    n15 --> n70
    n10 -.->|"An organisation"| n16
    n16 -.->|"Continue"| n17
    n17 -.->|"Find address"| n18
    n18 -.->|"1 Fictional Road / 2 Fake Way"| n19
    n18 -.->|"I want to manually enter my address"| n20
    n18 -.->|"Search again"| n17
    n14 -.->|"I want to manually enter my address"| n21
    n21 -.->|"Continue"| n15
    n22 --> n8
    n23 --> n24
    n24 -->|"Find address"| n25
    n24 -->|"Postcode: ZZ9 9ZZ"| n47
    n25 -->|"1 Fictional Road / 2 Fake Way"| n26
    n25 -->|"I want to manually enter my address"| n48
    n25 -->|"Search again"| n24
    n26 --> n27
    n27 --> n28
    n28 -->|"Company / Charity / None of these"| n29
    n28 -->|"Trust"| n49
    n29 -->|"Yes"| n30
    n29 -->|"No"| n32
    n30 -->|"Charities Commission of England and Wales"| n31
    n30 -->|"Charities Commission of Northern Ireland"| n55
    n30 -->|"Scottish Charity Regulator"| n56
    n30 -->|"None of these"| n32
    n31 --> n32
    n32 -->|"Yes"| n33
    n32 -->|"No"| n57
    n33 --> n34
    n34 --> n15
    n72 -.->|"Adding trust"| n35
    n35 -.->|"Continue with the change"| n36
    n36 -.->|"Continue"| n37
    n37 -.->|"Continue"| n38
    n30 -.->|"Charities Commission of Northern Ireland"| n39
    n30 -.->|"Scottish Charity Regulator"| n40
    n39 -.->|"Continue"| n15
    n40 -.->|"Continue"| n15
    n73 -.->|"Changed to non company"| n41
    n41 -.->|"Continue to other sections"| n74
    n74 -.->|"No members"| n42
    n42 -.->|"Director / Trustee and 2 more"| n43
    n43 -.->|"Continue"| n44
    n45 -->|"Enter address manually"| n46
    n45 -->|"search again."| n13
    n46 --> n15
    n47 -->|"Enter address manually"| n48
    n47 -->|"search again."| n24
    n48 --> n26
    n49 --> n50
    n50 --> n51
    n51 --> n52
    n52 --> n53
    n53 -->|"Find address"| n54
    n53 -->|"Postcode: ZZ9 9ZZ"| n65
    n54 -->|"1 Fictional Road / 2 Fake Way"| n29
    n54 -->|"I want to manually enter my address"| n66
    n54 -->|"Search again"| n53
    n55 --> n32
    n56 --> n32
    n57 -->|"Continue"| n75
    n75 -->|"No members"| n59
    n57 -->|"I don’t have these details"| n58
    n59 --> n60
    n60 --> n61
    n61 --> n62
    n62 -->|"Find address"| n63
    n62 -->|"Postcode: ZZ9 9ZZ"| n67
    n63 -->|"1 Fictional Road / 2 Fake Way"| n64
    n63 -->|"I want to manually enter my address"| n68
    n63 -->|"Search again"| n62
    n64 -->|"Continue"| n76
    n76 -->|"Some members"| n34
    n64 -->|"Add another person / Change Person 1"| n59
    n64 -->|"Remove Person 1"| n77
    n77 --> n57
    n65 -->|"Enter address manually"| n66
    n65 -->|"search again."| n53
    n66 --> n29
    n67 -->|"Enter address manually"| n68
    n67 -->|"search again."| n62
    n68 --> n64
    n78 -->|"Valid"| n0
    n78 -->|"Invalid"| n69
    n77 -->|"No members"| n76
    n77 -.->|"Some members (not walked)"| n64
    n75 -.->|"Some members (not walked)"| n64
    n76 -->|"No members"| n57
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n1 terminal
    class n3 terminal
    class n16 change
    class n17 change
    class n18 change
    class n19 change
    class n20 change
    class n21 change
    class n35 change
    class n36 change
    class n37 change
    class n38 change
    class n39 change
    class n40 change
    class n41 change
    class n42 change
    class n43 change
    class n44 change
    class n70 decision
    class n71 decision
    class n72 decision
    class n73 decision
    class n74 decision
    class n75 decision
    class n76 decision
    class n77 decision
    class n78 decision
    class n78 entryPoint
```

## Pages

### Tell us if you’re a joint landlord for a property

`accept-or-reject` · AcceptOrRejectStepConfig

[![v1](img/accept-or-reject/v1.thumb.jpg)](img/accept-or-reject/v1.jpg)

Shown on the default route


### Invite rejected

`/landlord/joint-landlord-invitation/invitation-rejected-confirmation`

[![v1](img/landlord_joint-landlord-invitation_invitation-rejected-confirmation/v1.thumb.jpg)](img/landlord_joint-landlord-invitation_invitation-rejected-confirmation/v1.jpg)

Shown when Tell us if you’re a joint landlord for a property: No


### Confirm that you’re a joint landlord for this property

`confirm-landlord-for-property` · ConfirmYouAreALandlordForThisPropertyStepConfig

[![v1](img/confirm-landlord-for-property/v1.thumb.jpg)](img/confirm-landlord-for-property/v1.jpg)

Shown on the default route

[![v2](img/confirm-landlord-for-property/v2.thumb.jpg)](img/confirm-landlord-for-property/v2.jpg)

Shown when Starting point: Invited user is not a landlord


### Registered as a joint landlord

`/landlord/joint-landlord-invitation/property-joined-confirmation`

[![v1](img/landlord_joint-landlord-invitation_property-joined-confirmation/v1.thumb.jpg)](img/landlord_joint-landlord-invitation_property-joined-confirmation/v1.jpg)

Shown on the default route


### Your data privacy

`privacy-notice` · PrivacyNoticeStepConfig

[![v1](img/privacy-notice/v1.thumb.jpg)](img/privacy-notice/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### You have not proven your identity using GOV.UK One Login

`identity-not-verified` · IdentityNotVerifiedStepConfig

[![v1](img/identity-not-verified/v1.thumb.jpg)](img/identity-not-verified/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your full name?

`name` · NameStepConfig

[![v1](img/name/v1.thumb.jpg)](img/name/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your date of birth?

`date-of-birth` · DateOfBirthStepConfig

[![v1](img/date-of-birth/v1.thumb.jpg)](img/date-of-birth/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your email address?

`email` · EmailStepConfig

[![v1](img/email/v1.thumb.jpg)](img/email/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your phone number?

`phone-number` · PhoneNumberStepConfig

[![v1](img/phone-number/v1.thumb.jpg)](img/phone-number/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Are you registering as an individual or an organisation?

`landlord-type` · LandlordTypeStepConfig

[![v1](img/landlord-type/v1.thumb.jpg)](img/landlord-type/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Do you live in England or Wales?

`country-of-residence` · CountryOfResidenceStepConfig

[![v1](img/country-of-residence/v1.thumb.jpg)](img/country-of-residence/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### This private beta is only available to landlords who live in England or Wales

`neither-england-nor-wales-address` · NonEnglandOrWalesAddressStepConfig

[![v1](img/neither-england-nor-wales-address/v1.thumb.jpg)](img/neither-england-nor-wales-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Do you live in England or Wales?: No


### What is your contact address?

`lookup-address` · LookupAddressStepConfig

[![v1](img/lookup-address/v1.thumb.jpg)](img/lookup-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Select your address

`select-address` · SelectAddressStepConfig

[![v1](img/select-address/v1.thumb.jpg)](img/select-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Check your answers

`check-answers` · LandlordRegistrationCyaStepConfig

[![v1](img/check-answers/v1.thumb.jpg)](img/check-answers/v1.jpg)

Shown when Starting point: Invited user is not a landlord

[![v2](img/check-answers/v2.thumb.jpg)](img/check-answers/v2.jpg)

Shown when Starting point: Invited user is not a landlord; One Login identity check: Identity verified

[![v3](img/check-answers/v3.thumb.jpg)](img/check-answers/v3.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation

[![v4](img/check-answers/v4.thumb.jpg)](img/check-answers/v4.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust

[![v5](img/check-answers/v5.thumb.jpg)](img/check-answers/v5.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation a registered charity?: No

[![v6](img/check-answers/v6.thumb.jpg)](img/check-answers/v6.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Who is your charity registered with?: None of these

[![v7](img/check-answers/v7.thumb.jpg)](img/check-answers/v7.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No

[![v8](img/check-answers/v8.thumb.jpg)](img/check-answers/v8.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Trustee

[![v9](img/check-answers/v9.thumb.jpg)](img/check-answers/v9.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Partner

[![v10](img/check-answers/v10.thumb.jpg)](img/check-answers/v10.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; Who do you want to provide details for?: Other member of the governing body


### What is the name of your organisation?

`organisation-name` · OrgNameStepConfig

[![v1](img/change_organisation-name/v1.thumb.jpg)](img/change_organisation-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Landlord type; Are you registering as an individual or an organisation?: An organisation (when changing an answer)


### What is your organisation’s address?

`organisation-address/lookup-address` · LookupAddressStepConfig

[![v1](img/change_organisation-address_lookup-address/v1.thumb.jpg)](img/change_organisation-address_lookup-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Landlord type (when changing an answer)


### Select your organisation’s address

`organisation-address/select-address` · SelectAddressStepConfig

[![v1](img/change_organisation-address_select-address/v1.thumb.jpg)](img/change_organisation-address_select-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Landlord type (when changing an answer)


### What is your organisation’s email address?

`organisation-email` · OrgEmailStepConfig

[![v1](img/change_organisation-email/v1.thumb.jpg)](img/change_organisation-email/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Landlord type; Select your organisation’s address: 1 Fictional Road (when changing an answer)


### What is your organisation’s address?

`organisation-address/manual-address` · ManualAddressStepConfig

[![v1](img/change_organisation-address_manual-address/v1.thumb.jpg)](img/change_organisation-address_manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Landlord type; Select your organisation’s address: I want to manually enter my address (when changing an answer)


### What is your contact address?

`manual-address` · ManualAddressStepConfig

[![v1](img/change_manual-address/v1.thumb.jpg)](img/change_manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Check your answers: Change Contact address; Select your address: I want to manually enter my address (when changing an answer)


### Confirm your name and date of birth

`confirm-identity` · ConfirmIdentityStepConfig

[![v1](img/confirm-identity/v1.thumb.jpg)](img/confirm-identity/v1.jpg)

Shown when Starting point: Invited user is not a landlord; One Login identity check: Identity verified


### What is the name of your organisation?

`organisation-name` · OrgNameStepConfig

[![v1](img/organisation-name/v1.thumb.jpg)](img/organisation-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### What is your organisation’s address?

`organisation-address/lookup-address` · LookupAddressStepConfig

[![v1](img/organisation-address_lookup-address/v1.thumb.jpg)](img/organisation-address_lookup-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Select your organisation’s address

`organisation-address/select-address` · SelectAddressStepConfig

[![v1](img/organisation-address_select-address/v1.thumb.jpg)](img/organisation-address_select-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### What is your organisation’s email address?

`organisation-email` · OrgEmailStepConfig

[![v1](img/organisation-email/v1.thumb.jpg)](img/organisation-email/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### What is your organisation’s phone number?

`organisation-phone-number` · OrgPhoneNumberStepConfig

[![v1](img/organisation-phone-number/v1.thumb.jpg)](img/organisation-phone-number/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Select your organisation’s type

`organisation-type` · OrgTypeStepConfig

[![v1](img/organisation-type/v1.thumb.jpg)](img/organisation-type/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Is your organisation a registered charity?

`organisation-charity` · OrgIsRegisteredCharityStepConfig

[![v1](img/organisation-charity/v1.thumb.jpg)](img/organisation-charity/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Who is your charity registered with?

`organisation-charity-registered-with` · OrgCharityRegisteredWithStepConfig

[![v1](img/organisation-charity-registered-with/v1.thumb.jpg)](img/organisation-charity-registered-with/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### What is your organisation’s charity number?

`organisation-charity-number` · OrgCharityNumberEnglandAndWalesStepConfig

[![v1](img/organisation-charity-number/v1.thumb.jpg)](img/organisation-charity-number/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Is your organisation registered with Companies House?

`organisation-companies-house` · OrgIsRegisteredCompanyStepConfig

[![v1](img/organisation-companies-house/v1.thumb.jpg)](img/organisation-companies-house/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### What is your organisation’s company number?

`organisation-company-number` · OrgCompanyNumberStepConfig

[![v1](img/organisation-company-number/v1.thumb.jpg)](img/organisation-company-number/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### Who is the main contact for your organisation?

`organisation-main-contact` · OrgMainContactStepConfig

[![v1](img/organisation-main-contact/v1.thumb.jpg)](img/organisation-main-contact/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation


### You must provide trustee details

`organisation-type-trust-interruption` · OrgTypeTrustInterruptionStepConfig

[![v1](img/change_organisation-type-trust-interruption/v1.thumb.jpg)](img/change_organisation-type-trust-interruption/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Organisation type; Select your organisation’s type: Trust (when changing an answer)


### What is the lead trustee’s full name?

`lead-trustee-name` · LeadTrusteeNameStepConfig

[![v1](img/change_lead-trustee-name/v1.thumb.jpg)](img/change_lead-trustee-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Organisation type (when changing an answer)


### What is the lead trustee’s date of birth?

`lead-trustee-dob` · LeadTrusteeDobStepConfig

[![v1](img/change_lead-trustee-dob/v1.thumb.jpg)](img/change_lead-trustee-dob/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Organisation type (when changing an answer)


### What is the lead trustee’s email address?

`lead-trustee-email` · LeadTrusteeEmailStepConfig

[![v1](img/change_lead-trustee-email/v1.thumb.jpg)](img/change_lead-trustee-email/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Organisation type (when changing an answer)


### What is your organisation’s charity number?

`organisation-charity-number-northern-ireland` · OrgCharityNumberNorthernIrelandStepConfig

[![v1](img/change_organisation-charity-number-northern-ireland/v1.thumb.jpg)](img/change_organisation-charity-number-northern-ireland/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered charity; Who is your charity registered with?: Charities Commission of Northern Ireland (when changing an answer)


### What is your organisation’s charity number?

`organisation-charity-number-scotland` · OrgCharityNumberScotlandStepConfig

[![v1](img/change_organisation-charity-number-scotland/v1.thumb.jpg)](img/change_organisation-charity-number-scotland/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered charity; Who is your charity registered with?: Scottish Charity Regulator (when changing an answer)


### Your new answer affects other sections of your landlord record

`organisation-companies-house-interruption` · OrgCompaniesHouseInterruptionStepConfig

[![v1](img/change_organisation-companies-house-interruption/v1.thumb.jpg)](img/change_organisation-companies-house-interruption/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered with Companies House; Is your organisation registered with Companies House?: No (when changing an answer)


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/change_organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/change_organisation-governing-body-who-to-provide/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered with Companies House (when changing an answer)


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/change_organisation-governing-body-member-name/v1.thumb.jpg)](img/change_organisation-governing-body-member-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered with Companies House; Who do you want to provide details for?: Director (when changing an answer)


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/change_organisation-governing-body-member-dob/v1.thumb.jpg)](img/change_organisation-governing-body-member-dob/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Check your answers: Change Registered with Companies House (when changing an answer)


### No matching address in England or Wales found for ZZ9 9ZZ and 1

`no-address-found` · NoAddressFoundStepConfig

[![v1](img/no-address-found/v1.thumb.jpg)](img/no-address-found/v1.jpg)

Shown when Starting point: Invited user is not a landlord; What is your contact address?: Postcode: ZZ9 9ZZ


### What is your contact address?

`manual-address` · ManualAddressStepConfig

[![v1](img/manual-address/v1.thumb.jpg)](img/manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; What is your contact address?: Postcode: ZZ9 9ZZ


### No matching address in England or Wales found for ZZ9 9ZZ and 1

`organisation-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/organisation-address_no-address-found/v1.thumb.jpg)](img/organisation-address_no-address-found/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; What is your organisation’s address?: Postcode: ZZ9 9ZZ


### What is your organisation’s address?

`organisation-address/manual-address` · ManualAddressStepConfig

[![v1](img/organisation-address_manual-address/v1.thumb.jpg)](img/organisation-address_manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; What is your organisation’s address?: Postcode: ZZ9 9ZZ


### What is the lead trustee’s full name?

`lead-trustee-name` · LeadTrusteeNameStepConfig

[![v1](img/lead-trustee-name/v1.thumb.jpg)](img/lead-trustee-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### What is the lead trustee’s date of birth?

`lead-trustee-dob` · LeadTrusteeDobStepConfig

[![v1](img/lead-trustee-dob/v1.thumb.jpg)](img/lead-trustee-dob/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### What is the lead trustee’s email address?

`lead-trustee-email` · LeadTrusteeEmailStepConfig

[![v1](img/lead-trustee-email/v1.thumb.jpg)](img/lead-trustee-email/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### What is the lead trustee’s phone number?

`lead-trustee-phone` · LeadTrusteePhoneStepConfig

[![v1](img/lead-trustee-phone/v1.thumb.jpg)](img/lead-trustee-phone/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### What is the lead trustee’s contact address?

`lead-trustee-address/lookup-address` · LookupAddressStepConfig

[![v1](img/lead-trustee-address_lookup-address/v1.thumb.jpg)](img/lead-trustee-address_lookup-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### Select your address

`lead-trustee-address/select-address` · SelectAddressStepConfig

[![v1](img/lead-trustee-address_select-address/v1.thumb.jpg)](img/lead-trustee-address_select-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust


### What is your organisation’s charity number?

`organisation-charity-number-northern-ireland` · OrgCharityNumberNorthernIrelandStepConfig

[![v1](img/organisation-charity-number-northern-ireland/v1.thumb.jpg)](img/organisation-charity-number-northern-ireland/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Who is your charity registered with?: Charities Commission of Northern Ireland


### What is your organisation’s charity number?

`organisation-charity-number-scotland` · OrgCharityNumberScotlandStepConfig

[![v1](img/organisation-charity-number-scotland/v1.thumb.jpg)](img/organisation-charity-number-scotland/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Who is your charity registered with?: Scottish Charity Regulator


### Providing details about your organisation’s governing body

`organisation-governing-body-details` · OrgGovBodyDetailsStepConfig

[![v1](img/organisation-governing-body-details/v1.thumb.jpg)](img/organisation-governing-body-details/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### You need to provide more information to register

`organisation-governing-body-must-provide-info` · OrgGovBodyMustProvideInfoStepConfig

[![v1](img/organisation-governing-body-must-provide-info/v1.thumb.jpg)](img/organisation-governing-body-must-provide-info/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; Providing details about your organisation’s governing body: I don’t have these details


### Who do you want to provide details for?

`organisation-governing-body-who-to-provide` · OrgGovBodyWhoToProvideStepConfig

[![v1](img/organisation-governing-body-who-to-provide/v1.thumb.jpg)](img/organisation-governing-body-who-to-provide/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### What is their full name?

`organisation-governing-body-member-name` · OrgGovBodyMemberNameStepConfig

[![v1](img/organisation-governing-body-member-name/v1.thumb.jpg)](img/organisation-governing-body-member-name/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### What is their date of birth?

`organisation-governing-body-member-dob` · OrgGovBodyMemberDobStepConfig

[![v1](img/organisation-governing-body-member-dob/v1.thumb.jpg)](img/organisation-governing-body-member-dob/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### What is their contact address?

`organisation-governing-body-member-address/lookup-address` · LookupAddressStepConfig

[![v1](img/organisation-governing-body-member-address_lookup-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_lookup-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### Select their address

`organisation-governing-body-member-address/select-address` · SelectAddressStepConfig

[![v1](img/organisation-governing-body-member-address_select-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_select-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### You’ve added 1 person

`organisation-governing-body-member-list` · OrgGovBodyMemberListStepConfig

[![v1](img/organisation-governing-body-member-list/v1.thumb.jpg)](img/organisation-governing-body-member-list/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No


### No matching address in England or Wales found for ZZ9 9ZZ and 1

`lead-trustee-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/lead-trustee-address_no-address-found/v1.thumb.jpg)](img/lead-trustee-address_no-address-found/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust; What is the lead trustee’s contact address?: Postcode: ZZ9 9ZZ


### What is the lead trustee’s contact address?

`lead-trustee-address/manual-address` · ManualAddressStepConfig

[![v1](img/lead-trustee-address_manual-address/v1.thumb.jpg)](img/lead-trustee-address_manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Select your organisation’s type: Trust; What is the lead trustee’s contact address?: Postcode: ZZ9 9ZZ


### No matching address in England or Wales found for ZZ9 9ZZ and 1

`organisation-governing-body-member-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/organisation-governing-body-member-address_no-address-found/v1.thumb.jpg)](img/organisation-governing-body-member-address_no-address-found/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; What is their contact address?: Postcode: ZZ9 9ZZ


### What is their contact address?

`organisation-governing-body-member-address/manual-address` · ManualAddressStepConfig

[![v1](img/organisation-governing-body-member-address_manual-address/v1.thumb.jpg)](img/organisation-governing-body-member-address_manual-address/v1.jpg)

Shown when Starting point: Invited user is not a landlord; Are you registering as an individual or an organisation?: An organisation; Is your organisation registered with Companies House?: No; What is their contact address?: Postcode: ZZ9 9ZZ


### There was a problem with this invitation link

`invite-unavailable` · InviteUnavailableStepConfig

[![v1](img/invite-unavailable/v1.thumb.jpg)](img/invite-unavailable/v1.jpg)

Shown when Starting point: Invitation has expired

