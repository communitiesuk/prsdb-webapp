# Register a property journey map

Generated 2026-11-03T12:27:59.318447800Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c0["Property details"]
        subgraph c1["Property details"]
            subgraph c2["Property registration address"]
                n1["What is the property address? (2 variants)"]
                n2["Select your address (2 variants)"]
                n3["This property is registered on the database (2 variants)"]
                n71["No matching address in England found for ZZ9 9ZZ and Slate House"]
                n72["What is the property address?"]
                n73["What local council area is your property in? (2 variants)"]
            end
            n4["What type of property are you registering? (2 variants)"]
            n5["How many bedrooms in your property? (2 variants)"]
        end
    end
    subgraph c3["Ownership and landlords"]
        subgraph c4["Ownership and landlords task before associated parties"]
            subgraph c5["Joint landlords property registration"]
                subgraph c6["Invite joint landlords"]
                    n8["Invite a joint landlord to this property (2 variants)"]
                    n9["You’ve added 1 joint landlord (3 variants)"]
                    n74["Invite a joint landlord to this property"]
                    n75["Are you sure you want to remove jane.smith@example.com? (2 variants)"]
                    n111{"Has any joint landlords invited"}
                end
                n7["Invite joint landlords (2 variants)"]
                n110{"Has any joint landlords invited"}
            end
            n6["Select the type of ownership you have for your property (2 variants)"]
        end
    end
    subgraph c7["Who the council should contact"]
        subgraph c8["Correspondence"]
            subgraph c9["Correspondence address"]
                n11["Where the council should send post about this property (2 variants)"]
                n12["Select a postal address (2 variants)"]
                n76["No matching address in England or Wales found for ZZ9 9ZZ and Slate House"]
                n77["Enter a postal address"]
            end
            n10["Where should the council send emails? (2 variants)"]
        end
    end
    subgraph c10["Tell us if your property’s occupied"]
        n13["Is your property occupied by tenants? (2 variants)"]
    end
    subgraph c11["Who provides details"]
        n14["Who will provide details about how your property’s rented out"]
        n78["Allow your letting agent or property manager to provide details"]
    end
    subgraph c12["Tell us if your property needs a license"]
        subgraph c13["Licensing"]
            n15["Select the type of licence you have for your property (2 variants)"]
            n16["What is your selective licence number? (2 variants)"]
            n79["Provide details about property licensing later"]
            n80["What is your HMO mandatory licence number?"]
            n81["What is your HMO additional licence number?"]
        end
    end
    subgraph c14["Gas safety certificate"]
        subgraph c15["Gas safety"]
            subgraph c16["Gas safety details"]
                n17["Does the property have a gas supply or any gas appliances? (2 variants)"]
                n18["Do you have a gas safety certificate for this property? (2 variants)"]
                n19["What’s the issue date on the gas safety certificate? (2 variants)"]
                n20["Upload the gas safety certificate (2 variants)"]
                n21["You’ve uploaded 1 file (6 variants)"]
                n82["Provide these details later"]
                n83["You must get a valid gas safety certificate for this property"]
                n84["This gas safety certificate has expired"]
                n85["Are you sure you want to remove validFile.png?"]
                n112{"Has any in collection"}
            end
            n22["Gas safety certificate (4 variants)"]
        end
    end
    subgraph c17["Electrical safety certificate"]
        subgraph c18["Electrical safety"]
            subgraph c19["Electrical safety details"]
                n23["Which electrical safety certificate do you have for this property? (2 variants)"]
                n24["What’s the expiry date on the Electrical Installation Certificate? (2 variants)"]
                n25["Upload the Electrical Installation Certificate (EIC) (2 variants)"]
                n26["You’ve uploaded 1 file (7 variants)"]
                n86["Provide your electrical safety certificate later"]
                n87["What’s the expiry date on the Electrical Installation Condition Report?"]
                n88["Upload the Electrical Installation Condition Report (EICR)"]
                n89["You’ve uploaded 1 file (5 variants)"]
                n90["You must get a valid electrical safety certificate for this property"]
                n91["This electrical safety certificate has expired"]
                n92["Are you sure you want to remove validFile.png?"]
                n105["This electrical safety certificate has expired"]
                n106["Are you sure you want to remove validFile.png?"]
                n113{"Has any in collection"}
                n119{"Has any in collection"}
                n121{"Has any in collection"}
            end
            n27["Electrical safety certificate (3 variants)"]
        end
    end
    subgraph c20["Energy performance certificate (EPC)"]
        subgraph c21["Epc"]
            subgraph c22["Epc details"]
                n28["Do you have an EPC for this property? (2 variants)"]
                n29["Find your EPC (2 variants)"]
                n30["Confirm your EPC (2 variants)"]
                n93["Check this EPC"]
                n94["Confirm when the current tenancy began"]
                n95["You need a registered energy efficiency exemption to let this property"]
                n96["Which energy efficiency exemption did you register for this property?"]
                n97["Provide your EPC details later"]
                n98["Is an EPC required to let this property?"]
                n99["Your property is missing an EPC (2 variants)"]
                n100["We could not find your EPC"]
                n101["There’s a newer EPC for this property"]
                n102["This property’s EPC has expired (2 variants)"]
                n107["This property does not meet energy efficiency requirements for letting"]
                n108["Why does this property not need an EPC to be let?"]
                n114{"Epc lookup by uprn"}
                n115{"Epc age check"}
                n116{"Epc energy rating check"}
                n122{"Property occupied check"}
            end
            n31["Energy performance certificate (EPC) (8 variants)"]
        end
    end
    subgraph c23["Tenancy details"]
        subgraph c24["Tenancy details"]
            subgraph c25["Households and tenants"]
                n32["Households in your property (2 variants)"]
                n33["How many people live in your property? (2 variants)"]
                n103["Provide tenancy details later"]
            end
            subgraph c26["Rent includes bills"]
                n34["Does the rent include bills? (2 variants)"]
                n35["Which of these do you include in the rent? (2 variants)"]
            end
            subgraph c27["Rent frequency and amount"]
                n37["When you charge rent (2 variants)"]
                n38["What is the monthly rent? (6 variants)"]
            end
            n36["Is the property furnished? (2 variants)"]
        end
    end
    subgraph c28["Submit and pay"]
        n39["Check your answers for: Slate House, PRSDB Square, AA3 1AB (11 variants)"]
        n68["Property fee"]
        n70["Your payment has been declined"]
        n104["Your payment is processing"]
        n109["Confirm missing compliance certificates"]
        n117{"Has missing compliance"}
        n120{"Payment status check"}
    end
    subgraph c29["Changing answers"]
        n40["What is the property address?"]
        n41["What local council area is your property in?"]
        n42["Invite a joint landlord to this property"]
        n43["Are you sure you want to remove jane.smith@example.com? (2 variants)"]
        n44["You’ve added 2 joint landlords"]
        n45["Enter a postal address"]
        n46["Are you sure you want to change this?"]
        n47["Allow your letting agent or property manager to provide details"]
        n48["Provide details about property licensing later"]
        n49["What is your HMO mandatory licence number?"]
        n50["What is your HMO additional licence number?"]
        n51["Provide these details later"]
        n52["You must get a valid gas safety certificate for this property"]
        n53["Are you sure you want to remove validFile.png?"]
        n54["Provide your electrical safety certificate later"]
        n55["What’s the expiry date on the Electrical Installation Condition Report?"]
        n56["You must get a valid electrical safety certificate for this property"]
        n57["Upload the Electrical Installation Condition Report (EICR)"]
        n58["Are you sure you want to remove validFile.png?"]
        n59["You’ve uploaded 1 file"]
        n60["You’ve uploaded 2 files"]
        n61["Are you sure you want to remove validFile.png?"]
        n62["Provide your EPC details later"]
        n63["Is an EPC required to let this property?"]
        n64["Your property is missing an EPC"]
        n65["Why does this property not need an EPC to be let?"]
        n66["Provide tenancy details later"]
        n67["What is the 4-weekly rent? (2 variants)"]
    end
    n0["Register a rental property"]
    n69(["Property registered (7 variants)"])
    n118{"Who provides update routing"}
    journeyStart --> n0
    n0 -->|"Property details"| n1
    n1 -->|"Find address"| n2
    n1 -->|"Postcode: ZZ9 9ZZ"| n71
    n2 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB / Grate House, PRSDB Road, AA3 1AB"| n4
    n2 -->|"Registered House, PRSDB Road, AA3 1AB / Stage House, PRSDB Road, AA3 1AB"| n3
    n2 -->|"I want to manually enter my address"| n72
    n2 -->|"Search again"| n1
    n4 --> n5
    n5 --> n6
    n6 --> n110
    n110 -->|"No landlords"| n7
    n7 -->|"Yes"| n111
    n111 -->|"No landlords"| n8
    n7 -->|"No, I am the only landlord for this property"| n10
    n8 --> n9
    n9 -->|"Save and continue"| n10
    n9 -->|"Add another landlord / Change Joint landlord 1 / Change Joint landlord 2"| n74
    n9 -->|"Remove Joint landlord 1 / Remove Joint landlord 2"| n75
    n10 --> n11
    n11 -->|"Find address"| n12
    n11 -->|"Postcode: ZZ9 9ZZ"| n76
    n12 -->|"Slate House, PRSDB Square, AA3 1AB / Slate House, PRSDB Road, AA3 1AB and 3 more"| n13
    n12 -->|"I want to manually enter my address"| n77
    n12 -->|"Search again"| n11
    n13 -->|"Yes"| n14
    n13 -->|"No"| n15
    n14 -->|"I will provide these details"| n15
    n14 -->|"My letting agent or property manager"| n78
    n15 -->|"Selective licence, Save and continue"| n16
    n15 -->|"Selective licence, Provide this later / HMO mandatory licence, Provide this later and 2 more"| n79
    n15 -->|"HMO mandatory licence, Save and continue"| n80
    n15 -->|"HMO additional licence, Save and continue"| n81
    n15 -->|"This property does not need a licence, Save and continue"| n17
    n16 --> n17
    n17 -->|"Yes, Save and continue"| n18
    n17 -->|"Yes, Provide this later / No, Provide this later"| n82
    n17 -->|"No, Save and continue"| n22
    n18 -->|"Yes, Save and continue"| n19
    n18 -->|"No, Save and continue"| n83
    n19 -->|"Save and continue"| n112
    n112 -->|"No members"| n20
    n19 -->|"Date: 3 November 2027"| n84
    n20 --> n21
    n21 -->|"Save and continue"| n22
    n21 -->|"Upload another file"| n20
    n21 -->|"Remove validFile.png"| n85
    n22 -->|"Save and continue"| n0
    n22 -->|"Change Does the property have a gas supply or any gas appliances?"| n17
    n22 -->|"Change Do you have a valid gas safety certificate for this property? / Change Do you have a gas safety certificate for this property?"| n18
    n22 -->|"Change Issue date"| n19
    n22 -->|"Change Your certificate"| n21
    n0 -->|"Ownership and landlords"| n6
    n0 -->|"Who the council should contact"| n10
    n0 -->|"Tell us if your property’s occupied"| n13
    n0 -->|"Who will provide these details"| n14
    n0 -->|"Tell us if your property needs a license"| n15
    n0 -->|"Gas safety certificate"| n17
    n0 -->|"Electrical safety certificate"| n23
    n23 -->|"Electrical Installation Certificate (EIC), Save and continue"| n24
    n23 -->|"Electrical Installation Certificate (EIC), Provide this later / Electrical Installation Condition Report (EICR), Provide this later / None of the above, Provide this later"| n86
    n23 -->|"Electrical Installation Condition Report (EICR), Save and continue"| n87
    n23 -->|"None of the above, Save and continue"| n90
    n24 -->|"Save and continue"| n113
    n113 -->|"No members"| n25
    n24 -->|"Date: 3 October 2026"| n91
    n25 --> n26
    n26 -->|"Save and continue"| n27
    n26 -->|"Upload another file"| n25
    n26 -->|"Remove validFile.png"| n92
    n27 -->|"Save and continue"| n0
    n27 -->|"Change Which electrical safety certificate do you have for this property?"| n23
    n27 -->|"Change Expiry date"| n24
    n27 -->|"Change Your certificate"| n26
    n0 -->|"Energy performance certificate (EPC)"| n114
    n114 -->|"Not found"| n28
    n114 -->|"Epc found"| n93
    n28 -->|"Yes, Save and continue"| n29
    n28 -->|"Yes, Provide details later / No (for example, the property does not require an EPC to be let), Provide details later"| n97
    n28 -->|"No (for example, the property does not require an EPC to be let), Save and continue"| n98
    n29 -->|"Search"| n30
    n29 -->|"Search"| n100
    n29 -->|"Search"| n101
    n30 -->|"Yes"| n115
    n115 -->|"Epc 10 years or newer"| n116
    n116 -->|"Epc meets energy requirements"| n31
    n30 -->|"No, search again"| n29
    n31 -->|"Save and continue"| n0
    n31 -->|"Change / Change Do you have an EPC for this property?"| n114
    n0 -->|"Tenancy details"| n32
    n32 -->|"Save and continue"| n33
    n32 -->|"Provide this later"| n103
    n33 --> n34
    n34 -->|"Yes"| n35
    n34 -->|"No"| n36
    n35 --> n36
    n36 --> n37
    n37 --> n38
    n38 --> n39
    n39 --> n117
    n117 -->|"Unoccupied or valid certificates or delegated"| n68
    n2 -.->|"I want to manually enter my address"| n40
    n40 -.->|"Continue"| n41
    n41 -.->|"ADUR DISTRICT COUNCIL"| n39
    n9 -.->|"Add another landlord / Change Joint landlord 1"| n42
    n9 -.->|"Remove Joint landlord 1"| n43
    n42 -.->|"Save and continue"| n44
    n43 -.->|"Yes"| n7
    n43 -.->|"No / Cancel"| n9
    n44 -.->|"Change Joint landlord 2"| n42
    n44 -.->|"Remove Joint landlord 2"| n43
    n12 -.->|"I want to manually enter my address"| n45
    n45 -.->|"Confirm and continue"| n39
    n118 -.->|"Changed to letting agent"| n46
    n46 -.->|"Continue with the change"| n47
    n47 -.->|"Save and continue"| n39
    n15 -.->|"Selective licence, Provide this later / HMO mandatory licence, Provide this later and 2 more"| n48
    n15 -.->|"HMO mandatory licence, Save and continue"| n49
    n15 -.->|"HMO additional licence, Save and continue"| n50
    n48 -.->|"Save and continue"| n39
    n49 -.->|"Save and continue"| n39
    n50 -.->|"Save and continue"| n39
    n17 -.->|"Yes, Provide this later / No, Provide this later"| n51
    n18 -.->|"No, Save and continue"| n52
    n51 -.->|"Continue"| n39
    n52 -.->|"Continue without a valid gas safety certificate"| n39
    n21 -.->|"Remove validFile.png"| n53
    n23 -.->|"Electrical Installation Certificate (EIC), Provide this later / Electrical Installation Condition Report (EICR), Provide this later / None of the above, Provide this later"| n54
    n23 -.->|"Electrical Installation Condition Report (EICR), Save and continue"| n55
    n23 -.->|"None of the above, Save and continue"| n56
    n54 -.->|"Save and continue"| n39
    n55 -.->|"Save and continue"| n119
    n119 -.->|"No members"| n57
    n56 -.->|"Continue without a valid electrical safety certificate"| n39
    n26 -.->|"Remove validFile.png"| n58
    n57 -.->|"Upload a file"| n59
    n25 -.->|"Upload a file"| n60
    n58 -.->|"Yes"| n25
    n58 -.->|"No / Cancel"| n26
    n59 -.->|"Save and continue"| n39
    n59 -.->|"Upload another file"| n57
    n59 -.->|"Remove validFile.png"| n61
    n28 -.->|"Yes, Provide details later / No (for example, the property does not require an EPC to be let), Provide details later"| n62
    n28 -.->|"No (for example, the property does not require an EPC to be let), Save and continue"| n63
    n62 -.->|"Save and continue"| n39
    n63 -.->|"Yes"| n64
    n63 -.->|"No"| n65
    n64 -.->|"Continue anyway"| n39
    n65 -.->|"It has protected architectural or historical merit / It’s smaller than 50 metres squared and 3 more"| n39
    n32 -.->|"Provide this later"| n66
    n66 -.->|"Save and continue"| n39
    n37 -.->|"4 weekly / Weekly"| n67
    n68 -->|"Go to payment"| n69
    n68 -->|"Go to payment"| n104
    n68 -->|"Go to payment"| n120
    n120 -->|"Failure"| n70
    n71 -->|"Enter address manually"| n72
    n71 -->|"search again."| n1
    n72 --> n73
    n73 --> n4
    n74 --> n9
    n75 -->|"Yes"| n7
    n75 -->|"No / Cancel / Yes"| n9
    n76 -->|"Enter address manually"| n77
    n76 -->|"search again."| n11
    n77 --> n13
    n78 --> n39
    n79 --> n17
    n80 --> n17
    n81 --> n17
    n82 --> n22
    n83 --> n22
    n84 -->|"Continue without a valid gas safety certificate"| n22
    n84 -->|"Change the issue date"| n19
    n85 -->|"Yes"| n20
    n85 -->|"No / Cancel / Yes"| n21
    n19 -->|"Save and continue"| n21
    n86 --> n27
    n87 -->|"Save and continue"| n121
    n121 -->|"No members"| n88
    n87 -->|"Date: 3 October 2026"| n105
    n88 --> n89
    n89 -->|"Save and continue"| n27
    n89 -->|"Upload another file"| n88
    n89 -->|"Remove validFile.png"| n106
    n27 -->|"Change Expiry date"| n87
    n27 -->|"Change Your certificate"| n89
    n90 --> n27
    n91 -->|"Continue without a valid electrical safety certificate"| n27
    n91 -->|"Change the expiry date"| n24
    n92 -->|"Yes"| n25
    n92 -->|"No / Cancel / Yes"| n26
    n24 -->|"Save and continue"| n26
    n93 -->|"Yes"| n115
    n93 -->|"No (use a different EPC or the property does not need one)"| n28
    n115 -->|"Epc older than 10 years"| n122
    n122 -->|"Yes"| n94
    n94 -->|"Yes"| n116
    n94 -->|"No"| n102
    n31 -->|"Change Was the EPC still in date when the current tenancy began?"| n94
    n116 -->|"Epc low energy rating"| n95
    n95 -->|"Yes"| n96
    n95 -->|"No"| n107
    n96 --> n31
    n31 -->|"Change Do you have a registered energy efficiency exemption for this property?"| n95
    n31 -->|"Change Registered exemption"| n96
    n97 --> n31
    n98 -->|"Yes"| n99
    n98 -->|"No"| n108
    n99 --> n31
    n31 -->|"Change Is an EPC required to let this property?"| n98
    n100 -->|"I do not have an EPC for this property"| n98
    n100 -->|"search again"| n29
    n101 -->|"Continue with most recent EPC"| n115
    n101 -->|"Search by certificate number again"| n29
    n102 --> n31
    n103 --> n39
    n104 -->|"Check payment status"| n104
    n105 -->|"Continue without a valid electrical safety certificate"| n27
    n105 -->|"Change the expiry date"| n87
    n106 -->|"Yes"| n88
    n106 -->|"No / Cancel / Yes"| n89
    n87 -->|"Save and continue"| n89
    n107 --> n31
    n108 --> n31
    n31 -->|"Change Why does this property not need an EPC?"| n108
    n0 -->|"Submit and pay"| n39
    n122 -->|"No"| n102
    n117 -->|"Occupied and has invalid certificates"| n109
    n109 -->|"Yes (at risk of prosecution or fine)"| n68
    n109 -->|"No, go back"| n39
    n104 -->|"Complete"| n120
    n110 -.->|"Some landlords (not walked)"| n111
    n111 -.->|"Some landlords (not walked)"| n9
    n112 -->|"Some members"| n21
    n113 -->|"Some members"| n26
    n121 -.->|"No members (not walked)"| n88
    n121 -.->|"Some members (not walked)"| n89
    n120 -->|"Success"| n69
    n120 -->|"In progress"| n104
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n40 change
    class n41 change
    class n42 change
    class n43 change
    class n44 change
    class n45 change
    class n46 change
    class n47 change
    class n48 change
    class n49 change
    class n50 change
    class n51 change
    class n52 change
    class n53 change
    class n54 change
    class n55 change
    class n56 change
    class n57 change
    class n58 change
    class n59 change
    class n60 change
    class n61 change
    class n62 change
    class n63 change
    class n64 change
    class n65 change
    class n66 change
    class n67 change
    class n69 terminal
    class n110 decision
    class n111 decision
    class n112 decision
    class n113 decision
    class n114 decision
    class n115 decision
    class n116 decision
    class n117 decision
    class n118 decision
    class n119 decision
    class n120 decision
    class n121 decision
    class n122 decision
```

## Pages

### Register a rental property

`task-list` · PropertyRegistrationTaskListStepConfig

[![v1](img/task-list/v1.thumb.jpg)](img/task-list/v1.jpg)

Shown on the default route


### What is the property address?

`lookup-address` · LookupAddressStepConfig

[![v1](img/lookup-address/v1.thumb.jpg)](img/lookup-address/v1.jpg)

Shown on the default route

[![v2](img/lookup-address/v2.thumb.jpg)](img/lookup-address/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Address (when changing an answer)


### Select your address

`select-address` · SelectAddressStepConfig

[![v1](img/select-address/v1.thumb.jpg)](img/select-address/v1.jpg)

Shown on the default route

[![v2](img/select-address/v2.thumb.jpg)](img/select-address/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Address (when changing an answer)


### This property is registered on the database

`already-registered` · AlreadyRegisteredStepConfig

[![v1](img/already-registered/v1.thumb.jpg)](img/already-registered/v1.jpg)

Shown when Select your address: Registered House, PRSDB Road, AA3 1AB

[![v2](img/already-registered/v2.thumb.jpg)](img/already-registered/v2.jpg)

Shown when Select your address: Stage House, PRSDB Road, AA3 1AB


### What type of property are you registering?

`property-type` · PropertyTypeStepConfig

[![v1](img/property-type/v1.thumb.jpg)](img/property-type/v1.jpg)

Shown on the default route

[![v2](img/property-type/v2.thumb.jpg)](img/property-type/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Property type (when changing an answer)


### How many bedrooms in your property?

`number-of-bedrooms` · BedroomsStepConfig

[![v1](img/number-of-bedrooms/v1.thumb.jpg)](img/number-of-bedrooms/v1.jpg)

Shown on the default route

[![v2](img/number-of-bedrooms/v2.thumb.jpg)](img/number-of-bedrooms/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Number of bedrooms (when changing an answer)


### Select the type of ownership you have for your property

`ownership-type` · OwnershipTypeStepConfig

[![v1](img/ownership-type/v1.thumb.jpg)](img/ownership-type/v1.jpg)

Shown on the default route

[![v2](img/ownership-type/v2.thumb.jpg)](img/ownership-type/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change How do you own this property? (when changing an answer)


### Invite joint landlords

`has-joint-landlords` · HasJointLandlordsConfig

[![v1](img/has-joint-landlords/v1.thumb.jpg)](img/has-joint-landlords/v1.jpg)

Shown on the default route

[![v2](img/has-joint-landlords/v2.thumb.jpg)](img/has-joint-landlords/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations; Are you sure you want to remove jane.smith@example.com?: Yes (when changing an answer)


### Invite a joint landlord to this property

`invite-joint-landlord` · InviteJointLandlordStepConfig

[![v1](img/invite-joint-landlord/v1.thumb.jpg)](img/invite-joint-landlord/v1.jpg)

Shown on the default route

[![v2](img/invite-joint-landlord/v2.thumb.jpg)](img/invite-joint-landlord/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations; Invite joint landlords: Yes (when changing an answer)


### You’ve added 1 joint landlord

`check-joint-landlords` · CheckJointLandlordsStepConfig

[![v1](img/check-joint-landlords/v1.thumb.jpg)](img/check-joint-landlords/v1.jpg)

Shown on the default route

[![v2](img/check-joint-landlords/v2.thumb.jpg)](img/check-joint-landlords/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations (when changing an answer)

[![v3](img/check-joint-landlords/v3.thumb.jpg)](img/check-joint-landlords/v3.jpg)

Shown when You’ve added 1 joint landlord: Add another landlord


### Where should the council send emails?

`correspondence-email` · CorrespondenceEmailStepConfig

[![v1](img/correspondence-email/v1.thumb.jpg)](img/correspondence-email/v1.jpg)

Shown on the default route

[![v2](img/correspondence-email/v2.thumb.jpg)](img/correspondence-email/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Email address (when changing an answer)


### Where the council should send post about this property

`correspondence-address/lookup-address` · LookupAddressStepConfig

[![v1](img/correspondence-address_lookup-address/v1.thumb.jpg)](img/correspondence-address_lookup-address/v1.jpg)

Shown on the default route

[![v2](img/correspondence-address_lookup-address/v2.thumb.jpg)](img/correspondence-address_lookup-address/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Postal address (when changing an answer)


### Select a postal address

`correspondence-address/select-address` · SelectAddressStepConfig

[![v1](img/correspondence-address_select-address/v1.thumb.jpg)](img/correspondence-address_select-address/v1.jpg)

Shown on the default route

[![v2](img/correspondence-address_select-address/v2.thumb.jpg)](img/correspondence-address_select-address/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Postal address (when changing an answer)


### Is your property occupied by tenants?

`occupancy` · OccupiedStepConfig

[![v1](img/occupancy/v1.thumb.jpg)](img/occupancy/v1.jpg)

Shown on the default route

[![v2](img/occupancy/v2.thumb.jpg)](img/occupancy/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Is this property occupied by tenants? (when changing an answer)


### Who will provide details about how your property’s rented out

`who-provides-rental-details` · WhoProvidesRentalDetailsStepConfig

[![v1](img/who-provides-rental-details/v1.thumb.jpg)](img/who-provides-rental-details/v1.jpg)

Shown on the default route


### Select the type of licence you have for your property

`licensing-type` · LicensingTypeStepConfig

[![v1](img/licensing-type/v1.thumb.jpg)](img/licensing-type/v1.jpg)

Shown on the default route

[![v2](img/licensing-type/v2.thumb.jpg)](img/licensing-type/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Licensing type (when changing an answer)


### What is your selective licence number?

`selective-licence` · SelectiveLicenceStepConfig

[![v1](img/selective-licence/v1.thumb.jpg)](img/selective-licence/v1.jpg)

Shown on the default route

[![v2](img/selective-licence/v2.thumb.jpg)](img/selective-licence/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Licensing type; Select the type of licence you have for your property: Selective licence, Save and continue (when changing an answer)


### Does the property have a gas supply or any gas appliances?

`has-gas-supply` · HasGasSupplyStepConfig

[![v1](img/has-gas-supply/v1.thumb.jpg)](img/has-gas-supply/v1.jpg)

Shown on the default route

[![v2](img/has-gas-supply/v2.thumb.jpg)](img/has-gas-supply/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances? (when changing an answer)


### Do you have a gas safety certificate for this property?

`has-gas-safety` · HasGasCertStepConfig

[![v1](img/has-gas-safety/v1.thumb.jpg)](img/has-gas-safety/v1.jpg)

Shown on the default route

[![v2](img/has-gas-safety/v2.thumb.jpg)](img/has-gas-safety/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; Does the property have a gas supply or any gas appliances?: Yes, Save and continue (when changing an answer)


### What’s the issue date on the gas safety certificate?

`gas-safety-certificate-issue-date` · GasCertIssueDateStepConfig

[![v1](img/gas-safety-certificate-issue-date/v1.thumb.jpg)](img/gas-safety-certificate-issue-date/v1.jpg)

Shown on the default route

[![v2](img/gas-safety-certificate-issue-date/v2.thumb.jpg)](img/gas-safety-certificate-issue-date/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; Do you have a gas safety certificate for this property?: Yes, Save and continue (when changing an answer)


### Upload the gas safety certificate

`gas-safety-certificate-file-upload` · UploadGasCertStepConfig

[![v1](img/gas-safety-certificate-file-upload/v1.thumb.jpg)](img/gas-safety-certificate-file-upload/v1.jpg)

Shown on the default route

[![v2](img/gas-safety-certificate-file-upload/v2.thumb.jpg)](img/gas-safety-certificate-file-upload/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; You’ve uploaded 1 file: Upload another file (when changing an answer)


### You’ve uploaded 1 file

`check-gas-safety-certificate-uploads` · CheckGasCertUploadsStepConfig

[![v1](img/check-gas-safety-certificate-uploads/v1.thumb.jpg)](img/check-gas-safety-certificate-uploads/v1.jpg)

Shown on the default route

[![v2](img/check-gas-safety-certificate-uploads/v2.thumb.jpg)](img/check-gas-safety-certificate-uploads/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances? (when changing an answer)

[![v3](img/check-gas-safety-certificate-uploads/v3.thumb.jpg)](img/check-gas-safety-certificate-uploads/v3.jpg)

Shown when You’ve uploaded 1 file: Upload another file

[![v4](img/check-gas-safety-certificate-uploads/v4.thumb.jpg)](img/check-gas-safety-certificate-uploads/v4.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png

[![v5](img/check-gas-safety-certificate-uploads/v5.thumb.jpg)](img/check-gas-safety-certificate-uploads/v5.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Upload another file

[![v6](img/check-gas-safety-certificate-uploads/v6.thumb.jpg)](img/check-gas-safety-certificate-uploads/v6.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Remove validFile.png


### Gas safety certificate

`check-gas-safety-answers` · CheckGasSafetyAnswersStepConfig

[![v1](img/check-gas-safety-answers/v1.thumb.jpg)](img/check-gas-safety-answers/v1.jpg)

Shown on the default route

[![v2](img/check-gas-safety-answers/v2.thumb.jpg)](img/check-gas-safety-answers/v2.jpg)

Shown when Does the property have a gas supply or any gas appliances?: Yes, Provide this later

[![v3](img/check-gas-safety-answers/v3.thumb.jpg)](img/check-gas-safety-answers/v3.jpg)

Shown when Does the property have a gas supply or any gas appliances?: No, Save and continue

[![v4](img/check-gas-safety-answers/v4.thumb.jpg)](img/check-gas-safety-answers/v4.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Save and continue


### Which electrical safety certificate do you have for this property?

`has-electrical-safety` · HasElectricalCertStepConfig

[![v1](img/has-electrical-safety/v1.thumb.jpg)](img/has-electrical-safety/v1.jpg)

Shown on the default route

[![v2](img/has-electrical-safety/v2.thumb.jpg)](img/has-electrical-safety/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property? (when changing an answer)


### What’s the expiry date on the Electrical Installation Certificate?

`eic-expiry-date` · EicExpiryDateStepConfig

[![v1](img/eic-expiry-date/v1.thumb.jpg)](img/eic-expiry-date/v1.jpg)

Shown on the default route

[![v2](img/eic-expiry-date/v2.thumb.jpg)](img/eic-expiry-date/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: Electrical Installation Certificate (EIC), Save and continue (when changing an answer)


### Upload the Electrical Installation Certificate (EIC)

`eic-file-upload` · UploadEicStepConfig

[![v1](img/eic-file-upload/v1.thumb.jpg)](img/eic-file-upload/v1.jpg)

Shown on the default route

[![v2](img/eic-file-upload/v2.thumb.jpg)](img/eic-file-upload/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; You’ve uploaded 1 file: Upload another file (when changing an answer)


### You’ve uploaded 1 file

`check-eic-uploads` · CheckEicUploadsStepConfig

[![v1](img/check-eic-uploads/v1.thumb.jpg)](img/check-eic-uploads/v1.jpg)

Shown on the default route

[![v2](img/check-eic-uploads/v2.thumb.jpg)](img/check-eic-uploads/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property? (when changing an answer)

[![v3](img/check-eic-uploads/v3.thumb.jpg)](img/check-eic-uploads/v3.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; Are you sure you want to remove validFile.png?: No (when changing an answer)

[![v4](img/check-eic-uploads/v4.thumb.jpg)](img/check-eic-uploads/v4.jpg)

Shown when You’ve uploaded 1 file: Upload another file

[![v5](img/check-eic-uploads/v5.thumb.jpg)](img/check-eic-uploads/v5.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png

[![v6](img/check-eic-uploads/v6.thumb.jpg)](img/check-eic-uploads/v6.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Upload another file

[![v7](img/check-eic-uploads/v7.thumb.jpg)](img/check-eic-uploads/v7.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Remove validFile.png


### Electrical safety certificate

`check-electrical-safety-answers` · CheckElectricalSafetyAnswersStepConfig

[![v1](img/check-electrical-safety-answers/v1.thumb.jpg)](img/check-electrical-safety-answers/v1.jpg)

Shown on the default route

[![v2](img/check-electrical-safety-answers/v2.thumb.jpg)](img/check-electrical-safety-answers/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Certificate (EIC), Provide this later

[![v3](img/check-electrical-safety-answers/v3.thumb.jpg)](img/check-electrical-safety-answers/v3.jpg)

Shown when Which electrical safety certificate do you have for this property?: None of the above, Save and continue


### Do you have an EPC for this property?

`has-epc` · HasEpcStepConfig

[![v1](img/has-epc/v1.thumb.jpg)](img/has-epc/v1.jpg)

Shown on the default route

[![v2](img/has-epc/v2.thumb.jpg)](img/has-epc/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change (when changing an answer)


### Find your EPC

`find-your-epc` · FindYourEpcStepConfig

[![v1](img/find-your-epc/v1.thumb.jpg)](img/find-your-epc/v1.jpg)

Shown on the default route

[![v2](img/find-your-epc/v2.thumb.jpg)](img/find-your-epc/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change; Do you have an EPC for this property?: Yes, Save and continue (when changing an answer)


### Confirm your EPC

`confirm-epc-details` · ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig

[![v1](img/confirm-epc-details/v1.thumb.jpg)](img/confirm-epc-details/v1.jpg)

Shown on the default route

[![v2](img/confirm-epc-details/v2.thumb.jpg)](img/confirm-epc-details/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change (when changing an answer)


### Energy performance certificate (EPC)

`check-epc-answers` · CheckEpcAnswersStepConfig

[![v1](img/check-epc-answers/v1.thumb.jpg)](img/check-epc-answers/v1.jpg)

Shown on the default route

[![v2](img/check-epc-answers/v2.thumb.jpg)](img/check-epc-answers/v2.jpg)

Shown when EPC register search by address: Expired EPC found

[![v3](img/check-epc-answers/v3.thumb.jpg)](img/check-epc-answers/v3.jpg)

Shown when EPC register search by address: Low rated EPC found

[![v4](img/check-epc-answers/v4.thumb.jpg)](img/check-epc-answers/v4.jpg)

Shown when Do you have an EPC for this property?: Yes, Provide details later

[![v5](img/check-epc-answers/v5.thumb.jpg)](img/check-epc-answers/v5.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue

[![v6](img/check-epc-answers/v6.thumb.jpg)](img/check-epc-answers/v6.jpg)

Shown when EPC register search by address: Expired EPC found

[![v7](img/check-epc-answers/v7.thumb.jpg)](img/check-epc-answers/v7.jpg)

Shown when EPC register search by address: Low rated EPC found; You need a registered energy efficiency exemption to let this property: No

[![v8](img/check-epc-answers/v8.thumb.jpg)](img/check-epc-answers/v8.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue; Is an EPC required to let this property?: No


### Households in your property

`number-of-households` · HouseholdStepConfig

[![v1](img/number-of-households/v1.thumb.jpg)](img/number-of-households/v1.jpg)

Shown on the default route

[![v2](img/number-of-households/v2.thumb.jpg)](img/number-of-households/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Number of households (when changing an answer)


### How many people live in your property?

`number-of-people` · TenantsStepConfig

[![v1](img/number-of-people/v1.thumb.jpg)](img/number-of-people/v1.jpg)

Shown on the default route

[![v2](img/number-of-people/v2.thumb.jpg)](img/number-of-people/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Number of households; Households in your property: Save and continue (when changing an answer)


### Does the rent include bills?

`rent-includes-bills` · RentIncludesBillsStepConfig

[![v1](img/rent-includes-bills/v1.thumb.jpg)](img/rent-includes-bills/v1.jpg)

Shown on the default route

[![v2](img/rent-includes-bills/v2.thumb.jpg)](img/rent-includes-bills/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Rent includes bills (when changing an answer)


### Which of these do you include in the rent?

`bills-included` · BillsIncludedStepConfig

[![v1](img/bills-included/v1.thumb.jpg)](img/bills-included/v1.jpg)

Shown on the default route

[![v2](img/bills-included/v2.thumb.jpg)](img/bills-included/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Rent includes bills; Does the rent include bills?: Yes (when changing an answer)


### Is the property furnished?

`property-furnished` · FurnishedStatusStepConfig

[![v1](img/property-furnished/v1.thumb.jpg)](img/property-furnished/v1.jpg)

Shown on the default route

[![v2](img/property-furnished/v2.thumb.jpg)](img/property-furnished/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Furniture provided (when changing an answer)


### When you charge rent

`rent-frequency` · RentFrequencyStepConfig

[![v1](img/rent-frequency/v1.thumb.jpg)](img/rent-frequency/v1.jpg)

Shown on the default route

[![v2](img/rent-frequency/v2.thumb.jpg)](img/rent-frequency/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change When rent is paid (when changing an answer)


### What is the monthly rent?

`rent-amount` · RentAmountStepConfig

[![v1](img/rent-amount/v1.thumb.jpg)](img/rent-amount/v1.jpg)

Shown on the default route

[![v2](img/rent-amount/v2.thumb.jpg)](img/rent-amount/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change When rent is paid; When you charge rent: Select how often you will receive rent payments. (when changing an answer)

[![v3](img/rent-amount/v3.thumb.jpg)](img/rent-amount/v3.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change When rent is paid; When you charge rent: Other (when changing an answer)

[![v4](img/rent-amount/v4.thumb.jpg)](img/rent-amount/v4.jpg)

Shown when When you charge rent: 4 weekly

[![v5](img/rent-amount/v5.thumb.jpg)](img/rent-amount/v5.jpg)

Shown when When you charge rent: Weekly

[![v6](img/rent-amount/v6.thumb.jpg)](img/rent-amount/v6.jpg)

Shown when When you charge rent: Other


### Check your answers for: Slate House, PRSDB Square, AA3 1AB

`check-answers` · PropertyRegistrationCyaStepConfig

[![v1](img/check-answers/v1.thumb.jpg)](img/check-answers/v1.jpg)

Shown on the default route

[![v2](img/check-answers/v2.thumb.jpg)](img/check-answers/v2.jpg)

Shown when Who will provide details about how your property’s rented out: My letting agent or property manager

[![v3](img/check-answers/v3.thumb.jpg)](img/check-answers/v3.jpg)

Shown when Households in your property: Provide this later

[![v4](img/check-answers/v4.thumb.jpg)](img/check-answers/v4.jpg)

Shown when Does the rent include bills?: No

[![v5](img/check-answers/v5.thumb.jpg)](img/check-answers/v5.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ

[![v6](img/check-answers/v6.thumb.jpg)](img/check-answers/v6.jpg)

Shown when Select your address: Slate House, PRSDB Road, AA3 1AB

[![v7](img/check-answers/v7.thumb.jpg)](img/check-answers/v7.jpg)

Shown when Invite joint landlords: No, I am the only landlord for this property

[![v8](img/check-answers/v8.thumb.jpg)](img/check-answers/v8.jpg)

Shown when Is your property occupied by tenants?: No

[![v9](img/check-answers/v9.thumb.jpg)](img/check-answers/v9.jpg)

Shown when Select the type of licence you have for your property: Selective licence, Provide this later

[![v10](img/check-answers/v10.thumb.jpg)](img/check-answers/v10.jpg)

Shown when Does the property have a gas supply or any gas appliances?: Yes, Provide this later

[![v11](img/check-answers/v11.thumb.jpg)](img/check-answers/v11.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Save and continue


### What is the property address?

`manual-address` · ManualAddressStepConfig

[![v1](img/change_manual-address/v1.thumb.jpg)](img/change_manual-address/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Address; Select your address: I want to manually enter my address (when changing an answer)


### What local council area is your property in?

`local-council` · LocalCouncilStepConfig

[![v1](img/change_local-council/v1.thumb.jpg)](img/change_local-council/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Address (when changing an answer)


### Invite a joint landlord to this property

`invite-another-joint-landlord` · InviteJointLandlordStepConfig

[![v1](img/change_invite-another-joint-landlord/v1.thumb.jpg)](img/change_invite-another-joint-landlord/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations; You’ve added 1 joint landlord: Add another landlord (when changing an answer)


### Are you sure you want to remove jane.smith@example.com?

`remove-joint-landlord` · RemoveJointLandlordAreYouSureStepConfig

[![v1](img/change_remove-joint-landlord/v1.thumb.jpg)](img/change_remove-joint-landlord/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations; You’ve added 1 joint landlord: Remove Joint landlord 1 (when changing an answer)

[![v2](img/change_remove-joint-landlord/v2.thumb.jpg)](img/change_remove-joint-landlord/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations; You’ve added 2 joint landlords: Remove Joint landlord 2 (when changing an answer)


### You’ve added 2 joint landlords

`check-joint-landlords` · CheckJointLandlordsStepConfig

[![v1](img/change_check-joint-landlords/v1.thumb.jpg)](img/change_check-joint-landlords/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Joint landlord invitations (when changing an answer)


### Enter a postal address

`correspondence-address/manual-address` · ManualAddressStepConfig

[![v1](img/change_correspondence-address_manual-address/v1.thumb.jpg)](img/change_correspondence-address_manual-address/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Postal address; Select a postal address: I want to manually enter my address (when changing an answer)


### Are you sure you want to change this?

`confirm-change-to-letting-agent` · ConfirmChangeToLettingAgentStepConfig

[![v1](img/change_confirm-change-to-letting-agent/v1.thumb.jpg)](img/change_confirm-change-to-letting-agent/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Who will provide this property’s rental details?; Who will provide details about how your property’s rented out: My letting agent or property manager (when changing an answer)


### Allow your letting agent or property manager to provide details

`letting-agent-email` · LettingAgentEmailStepConfig

[![v1](img/change_letting-agent-email/v1.thumb.jpg)](img/change_letting-agent-email/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Who will provide this property’s rental details? (when changing an answer)


### Provide details about property licensing later

`provide-licensing-later` · ProvideLicensingLaterStepConfig

[![v1](img/change_provide-licensing-later/v1.thumb.jpg)](img/change_provide-licensing-later/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Licensing type; Select the type of licence you have for your property: Selective licence, Provide this later (when changing an answer)


### What is your HMO mandatory licence number?

`hmo-mandatory-licence` · HmoMandatoryLicenceStepConfig

[![v1](img/change_hmo-mandatory-licence/v1.thumb.jpg)](img/change_hmo-mandatory-licence/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Licensing type; Select the type of licence you have for your property: HMO mandatory licence, Save and continue (when changing an answer)


### What is your HMO additional licence number?

`hmo-additional-licence` · HmoAdditionalLicenceStepConfig

[![v1](img/change_hmo-additional-licence/v1.thumb.jpg)](img/change_hmo-additional-licence/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Licensing type; Select the type of licence you have for your property: HMO additional licence, Save and continue (when changing an answer)


### Provide these details later

`provide-gas-safety-certificate-later` · ProvideGasCertLaterStepConfig

[![v1](img/change_provide-gas-safety-certificate-later/v1.thumb.jpg)](img/change_provide-gas-safety-certificate-later/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; Does the property have a gas supply or any gas appliances?: Yes, Provide this later (when changing an answer)


### You must get a valid gas safety certificate for this property

`gas-safety-certificate-missing` · GasCertMissingStepConfig

[![v1](img/change_gas-safety-certificate-missing/v1.thumb.jpg)](img/change_gas-safety-certificate-missing/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; Do you have a gas safety certificate for this property?: No, Save and continue (when changing an answer)


### Are you sure you want to remove validFile.png?

`remove-gas-safety-certificate-upload` · RemoveGasCertUploadStepConfig

[![v1](img/change_remove-gas-safety-certificate-upload/v1.thumb.jpg)](img/change_remove-gas-safety-certificate-upload/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Does the property have a gas supply or any gas appliances?; You’ve uploaded 1 file: Remove validFile.png (when changing an answer)


### Provide your electrical safety certificate later

`provide-electrical-safety-certificate-later` · ProvideElectricalCertLaterStepConfig

[![v1](img/change_provide-electrical-safety-certificate-later/v1.thumb.jpg)](img/change_provide-electrical-safety-certificate-later/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: Electrical Installation Certificate (EIC), Provide this later (when changing an answer)


### What’s the expiry date on the Electrical Installation Condition Report?

`eicr-expiry-date` · EicrExpiryDateStepConfig

[![v1](img/change_eicr-expiry-date/v1.thumb.jpg)](img/change_eicr-expiry-date/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue (when changing an answer)


### You must get a valid electrical safety certificate for this property

`electrical-safety-certificate-missing` · ElectricalCertMissingStepConfig

[![v1](img/change_electrical-safety-certificate-missing/v1.thumb.jpg)](img/change_electrical-safety-certificate-missing/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: None of the above, Save and continue (when changing an answer)


### Upload the Electrical Installation Condition Report (EICR)

`eicr-file-upload` · UploadEicrStepConfig

[![v1](img/change_eicr-file-upload/v1.thumb.jpg)](img/change_eicr-file-upload/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property? (when changing an answer)


### Are you sure you want to remove validFile.png?

`remove-eic-upload` · RemoveEicUploadStepConfig

[![v1](img/change_remove-eic-upload/v1.thumb.jpg)](img/change_remove-eic-upload/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; You’ve uploaded 1 file: Remove validFile.png (when changing an answer)


### You’ve uploaded 1 file

`check-eicr-uploads` · CheckEicrUploadsStepConfig

[![v1](img/change_check-eicr-uploads/v1.thumb.jpg)](img/change_check-eicr-uploads/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property? (when changing an answer)


### You’ve uploaded 2 files

`check-eic-uploads` · CheckEicUploadsStepConfig

[![v1](img/change_check-eic-uploads/v1.thumb.jpg)](img/change_check-eic-uploads/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property? (when changing an answer)


### Are you sure you want to remove validFile.png?

`remove-eicr-upload` · RemoveEicrUploadStepConfig

[![v1](img/change_remove-eicr-upload/v1.thumb.jpg)](img/change_remove-eicr-upload/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Which electrical safety certificate do you have for this property?; You’ve uploaded 1 file: Remove validFile.png (when changing an answer)


### Provide your EPC details later

`provide-epc-later` · ProvideEpcLaterStepConfig

[![v1](img/change_provide-epc-later/v1.thumb.jpg)](img/change_provide-epc-later/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change; Do you have an EPC for this property?: Yes, Provide details later (when changing an answer)


### Is an EPC required to let this property?

`is-epc-required` · IsEpcRequiredStepConfig

[![v1](img/change_is-epc-required/v1.thumb.jpg)](img/change_is-epc-required/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change; Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue (when changing an answer)


### Your property is missing an EPC

`epc-missing` · EpcMissingStepConfig

[![v1](img/change_epc-missing/v1.thumb.jpg)](img/change_epc-missing/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change; Is an EPC required to let this property?: Yes (when changing an answer)


### Why does this property not need an EPC to be let?

`epc-exemption` · EpcExemptionStepConfig

[![v1](img/change_epc-exemption/v1.thumb.jpg)](img/change_epc-exemption/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change; Is an EPC required to let this property?: No (when changing an answer)


### Provide tenancy details later

`provide-tenancy-details-later` · ProvideTenancyDetailsLaterStepConfig

[![v1](img/change_provide-tenancy-details-later/v1.thumb.jpg)](img/change_provide-tenancy-details-later/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change Number of households; Households in your property: Provide this later (when changing an answer)


### What is the 4-weekly rent?

`rent-amount` · RentAmountStepConfig

[![v1](img/change_rent-amount/v1.thumb.jpg)](img/change_rent-amount/v1.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change When rent is paid; When you charge rent: 4 weekly (when changing an answer)

[![v2](img/change_rent-amount/v2.thumb.jpg)](img/change_rent-amount/v2.jpg)

Shown when Check your answers for: Slate House, PRSDB Square, AA3 1AB: Change When rent is paid; When you charge rent: Weekly (when changing an answer)


### Property fee

`payment-summary` · PaymentSummaryStepConfig

[![v1](img/payment-summary/v1.thumb.jpg)](img/payment-summary/v1.jpg)

Shown on the default route


### Property registered

`/landlord/register-property/confirmation`

[![v1](img/landlord_register-property_confirmation/v1.thumb.jpg)](img/landlord_register-property_confirmation/v1.jpg)

Shown on the default route

[![v2](img/landlord_register-property_confirmation/v2.thumb.jpg)](img/landlord_register-property_confirmation/v2.jpg)

Shown when Who will provide details about how your property’s rented out: My letting agent or property manager

[![v3](img/landlord_register-property_confirmation/v3.thumb.jpg)](img/landlord_register-property_confirmation/v3.jpg)

Shown when Households in your property: Provide this later

[![v4](img/landlord_register-property_confirmation/v4.thumb.jpg)](img/landlord_register-property_confirmation/v4.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ

[![v5](img/landlord_register-property_confirmation/v5.thumb.jpg)](img/landlord_register-property_confirmation/v5.jpg)

Shown when Select your address: Slate House, PRSDB Road, AA3 1AB

[![v6](img/landlord_register-property_confirmation/v6.thumb.jpg)](img/landlord_register-property_confirmation/v6.jpg)

Shown when Select the type of licence you have for your property: Selective licence, Provide this later

[![v7](img/landlord_register-property_confirmation/v7.thumb.jpg)](img/landlord_register-property_confirmation/v7.jpg)

Shown when Does the property have a gas supply or any gas appliances?: Yes, Provide this later


### Your payment has been declined

`payment-failed` · PaymentFailedStepConfig

[![v1](img/payment-failed/v1.thumb.jpg)](img/payment-failed/v1.jpg)

Shown when GOV.UK Pay payment status: Payment failed


### No matching address in England found for ZZ9 9ZZ and Slate House

`no-address-found` · NoAddressFoundStepConfig

[![v1](img/no-address-found/v1.thumb.jpg)](img/no-address-found/v1.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ


### What is the property address?

`manual-address` · ManualAddressStepConfig

[![v1](img/manual-address/v1.thumb.jpg)](img/manual-address/v1.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ


### What local council area is your property in?

`local-council` · LocalCouncilStepConfig

[![v1](img/local-council/v1.thumb.jpg)](img/local-council/v1.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ

[![v2](img/local-council/v2.thumb.jpg)](img/local-council/v2.jpg)

Shown when What is the property address?: Postcode: ZZ9 9ZZ; Check your answers for: 1 Example Street, 1 Example Street, Exampleton, Example text, AA3 1AB: Change Local council (when changing an answer)


### Invite a joint landlord to this property

`invite-another-joint-landlord` · InviteJointLandlordStepConfig

[![v1](img/invite-another-joint-landlord/v1.thumb.jpg)](img/invite-another-joint-landlord/v1.jpg)

Shown when You’ve added 1 joint landlord: Add another landlord


### Are you sure you want to remove jane.smith@example.com?

`remove-joint-landlord` · RemoveJointLandlordAreYouSureStepConfig

[![v1](img/remove-joint-landlord/v1.thumb.jpg)](img/remove-joint-landlord/v1.jpg)

Shown when You’ve added 1 joint landlord: Remove Joint landlord 1

[![v2](img/remove-joint-landlord/v2.thumb.jpg)](img/remove-joint-landlord/v2.jpg)

Shown when You’ve added 1 joint landlord: Add another landlord; You’ve added 2 joint landlords: Remove Joint landlord 2


### No matching address in England or Wales found for ZZ9 9ZZ and Slate House

`correspondence-address/no-address-found` · NoAddressFoundStepConfig

[![v1](img/correspondence-address_no-address-found/v1.thumb.jpg)](img/correspondence-address_no-address-found/v1.jpg)

Shown when Where the council should send post about this property: Postcode: ZZ9 9ZZ


### Enter a postal address

`correspondence-address/manual-address` · ManualAddressStepConfig

[![v1](img/correspondence-address_manual-address/v1.thumb.jpg)](img/correspondence-address_manual-address/v1.jpg)

Shown when Where the council should send post about this property: Postcode: ZZ9 9ZZ


### Allow your letting agent or property manager to provide details

`letting-agent-email` · LettingAgentEmailStepConfig

[![v1](img/letting-agent-email/v1.thumb.jpg)](img/letting-agent-email/v1.jpg)

Shown when Who will provide details about how your property’s rented out: My letting agent or property manager


### Provide details about property licensing later

`provide-licensing-later` · ProvideLicensingLaterStepConfig

[![v1](img/provide-licensing-later/v1.thumb.jpg)](img/provide-licensing-later/v1.jpg)

Shown when Select the type of licence you have for your property: Selective licence, Provide this later


### What is your HMO mandatory licence number?

`hmo-mandatory-licence` · HmoMandatoryLicenceStepConfig

[![v1](img/hmo-mandatory-licence/v1.thumb.jpg)](img/hmo-mandatory-licence/v1.jpg)

Shown when Select the type of licence you have for your property: HMO mandatory licence, Save and continue


### What is your HMO additional licence number?

`hmo-additional-licence` · HmoAdditionalLicenceStepConfig

[![v1](img/hmo-additional-licence/v1.thumb.jpg)](img/hmo-additional-licence/v1.jpg)

Shown when Select the type of licence you have for your property: HMO additional licence, Save and continue


### Provide these details later

`provide-gas-safety-certificate-later` · ProvideGasCertLaterStepConfig

[![v1](img/provide-gas-safety-certificate-later/v1.thumb.jpg)](img/provide-gas-safety-certificate-later/v1.jpg)

Shown when Does the property have a gas supply or any gas appliances?: Yes, Provide this later


### You must get a valid gas safety certificate for this property

`gas-safety-certificate-missing` · GasCertMissingStepConfig

[![v1](img/gas-safety-certificate-missing/v1.thumb.jpg)](img/gas-safety-certificate-missing/v1.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Save and continue


### This gas safety certificate has expired

`gas-safety-certificate-expired` · GasCertExpiredStepConfig

[![v1](img/gas-safety-certificate-expired/v1.thumb.jpg)](img/gas-safety-certificate-expired/v1.jpg)

Shown when What’s the issue date on the gas safety certificate?: Date: 3 November 2027


### Are you sure you want to remove validFile.png?

`remove-gas-safety-certificate-upload` · RemoveGasCertUploadStepConfig

[![v1](img/remove-gas-safety-certificate-upload/v1.thumb.jpg)](img/remove-gas-safety-certificate-upload/v1.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png


### Provide your electrical safety certificate later

`provide-electrical-safety-certificate-later` · ProvideElectricalCertLaterStepConfig

[![v1](img/provide-electrical-safety-certificate-later/v1.thumb.jpg)](img/provide-electrical-safety-certificate-later/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Certificate (EIC), Provide this later


### What’s the expiry date on the Electrical Installation Condition Report?

`eicr-expiry-date` · EicrExpiryDateStepConfig

[![v1](img/eicr-expiry-date/v1.thumb.jpg)](img/eicr-expiry-date/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue


### Upload the Electrical Installation Condition Report (EICR)

`eicr-file-upload` · UploadEicrStepConfig

[![v1](img/eicr-file-upload/v1.thumb.jpg)](img/eicr-file-upload/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue


### You’ve uploaded 1 file

`check-eicr-uploads` · CheckEicrUploadsStepConfig

[![v1](img/check-eicr-uploads/v1.thumb.jpg)](img/check-eicr-uploads/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue

[![v2](img/check-eicr-uploads/v2.thumb.jpg)](img/check-eicr-uploads/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; You’ve uploaded 1 file: Upload another file

[![v3](img/check-eicr-uploads/v3.thumb.jpg)](img/check-eicr-uploads/v3.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; You’ve uploaded 1 file: Remove validFile.png

[![v4](img/check-eicr-uploads/v4.thumb.jpg)](img/check-eicr-uploads/v4.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Upload another file

[![v5](img/check-eicr-uploads/v5.thumb.jpg)](img/check-eicr-uploads/v5.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Remove validFile.png


### You must get a valid electrical safety certificate for this property

`electrical-safety-certificate-missing` · ElectricalCertMissingStepConfig

[![v1](img/electrical-safety-certificate-missing/v1.thumb.jpg)](img/electrical-safety-certificate-missing/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: None of the above, Save and continue


### This electrical safety certificate has expired

`eic-expired` · EicExpiredStepConfig

[![v1](img/eic-expired/v1.thumb.jpg)](img/eic-expired/v1.jpg)

Shown when What’s the expiry date on the Electrical Installation Certificate?: Date: 3 October 2026


### Are you sure you want to remove validFile.png?

`remove-eic-upload` · RemoveEicUploadStepConfig

[![v1](img/remove-eic-upload/v1.thumb.jpg)](img/remove-eic-upload/v1.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png


### Check this EPC

`check-epc-details` · ConfirmEpcRetrievedByUprnStepConfig

[![v1](img/check-epc-details/v1.thumb.jpg)](img/check-epc-details/v1.jpg)

Shown when EPC register search by address: EPC found


### Confirm when the current tenancy began

`epc-in-date-at-start-of-tenancy-check` · EpcInDateAtStartOfTenancyCheckStepConfig

[![v1](img/epc-in-date-at-start-of-tenancy-check/v1.thumb.jpg)](img/epc-in-date-at-start-of-tenancy-check/v1.jpg)

Shown when EPC register search by address: Expired EPC found


### You need a registered energy efficiency exemption to let this property

`has-mees-exemption` · HasMeesExemptionStepConfig

[![v1](img/has-mees-exemption/v1.thumb.jpg)](img/has-mees-exemption/v1.jpg)

Shown when EPC register search by address: Low rated EPC found


### Which energy efficiency exemption did you register for this property?

`mees-exemption` · MeesExemptionStepConfig

[![v1](img/mees-exemption/v1.thumb.jpg)](img/mees-exemption/v1.jpg)

Shown when EPC register search by address: Low rated EPC found


### Provide your EPC details later

`provide-epc-later` · ProvideEpcLaterStepConfig

[![v1](img/provide-epc-later/v1.thumb.jpg)](img/provide-epc-later/v1.jpg)

Shown when Do you have an EPC for this property?: Yes, Provide details later


### Is an EPC required to let this property?

`is-epc-required` · IsEpcRequiredStepConfig

[![v1](img/is-epc-required/v1.thumb.jpg)](img/is-epc-required/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue


### Your property is missing an EPC

`epc-missing` · EpcMissingStepConfig

[![v1](img/epc-missing/v1.thumb.jpg)](img/epc-missing/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue

[![v2](img/epc-missing/v2.thumb.jpg)](img/epc-missing/v2.jpg)

Shown when Is your property occupied by tenants?: No; EPC register search by certificate number: Certificate not found


### We could not find your EPC

`epc-not-found` · EpcNotFoundStepConfig

[![v1](img/epc-not-found/v1.thumb.jpg)](img/epc-not-found/v1.jpg)

Shown when EPC register search by certificate number: Certificate not found


### There’s a newer EPC for this property

`epc-superseded` · EpcSuperseededStepConfig

[![v1](img/epc-superseded/v1.thumb.jpg)](img/epc-superseded/v1.jpg)

Shown when EPC register search by certificate number: Superseded certificate found


### This property’s EPC has expired

`epc-expired` · EpcExpiredStepConfig

[![v1](img/epc-expired/v1.thumb.jpg)](img/epc-expired/v1.jpg)

Shown when EPC register search by address: Expired EPC found

[![v2](img/epc-expired/v2.thumb.jpg)](img/epc-expired/v2.jpg)

Shown when Is your property occupied by tenants?: No; EPC register search by address: Expired EPC found


### Provide tenancy details later

`provide-tenancy-details-later` · ProvideTenancyDetailsLaterStepConfig

[![v1](img/provide-tenancy-details-later/v1.thumb.jpg)](img/provide-tenancy-details-later/v1.jpg)

Shown when Households in your property: Provide this later


### Your payment is processing

`payment-return` · PaymentReturnStepConfig

[![v1](img/payment-return/v1.thumb.jpg)](img/payment-return/v1.jpg)

Shown when GOV.UK Pay payment status: Payment still processing


### This electrical safety certificate has expired

`eicr-expired` · EicrExpiredStepConfig

[![v1](img/eicr-expired/v1.thumb.jpg)](img/eicr-expired/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; What’s the expiry date on the Electrical Installation Condition Report?: Date: 3 October 2026


### Are you sure you want to remove validFile.png?

`remove-eicr-upload` · RemoveEicrUploadStepConfig

[![v1](img/remove-eicr-upload/v1.thumb.jpg)](img/remove-eicr-upload/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Save and continue; You’ve uploaded 1 file: Remove validFile.png


### This property does not meet energy efficiency requirements for letting

`low-energy-rating` · LowEnergyRatingStepConfig

[![v1](img/low-energy-rating/v1.thumb.jpg)](img/low-energy-rating/v1.jpg)

Shown when EPC register search by address: Low rated EPC found; You need a registered energy efficiency exemption to let this property: No


### Why does this property not need an EPC to be let?

`epc-exemption` · EpcExemptionStepConfig

[![v1](img/epc-exemption/v1.thumb.jpg)](img/epc-exemption/v1.jpg)

Shown when Do you have an EPC for this property?: No (for example, the property does not require an EPC to be let), Save and continue; Is an EPC required to let this property?: No


### Confirm missing compliance certificates

`confirm-missing-compliance` · ConfirmMissingComplianceStepConfig

[![v1](img/confirm-missing-compliance/v1.thumb.jpg)](img/confirm-missing-compliance/v1.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Save and continue

