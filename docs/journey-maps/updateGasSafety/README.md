# Update gas safety journey map

Generated 2026-10-23T09:36:28.568507300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n7["You must get a valid gas safety certificate for this property"]
        n8["Are you sure you want to remove validFile.png?"]
    end
    n0["Does the property have a gas supply or any gas appliances?"]
    n1["Do you have a gas safety certificate for this property?"]
    n2["What’s the issue date on the gas safety certificate?"]
    n3["Upload the gas safety certificate"]
    n4["You’ve uploaded 1 file (5 variants)"]
    n5["Gas safety certificate (4 variants)"]
    n6(["1 Example Road EG1 1AA (7 variants)"])
    n9["You must get a valid gas safety certificate for this property (2 variants)"]
    n10["This gas safety certificate has expired"]
    n11["Are you sure you want to remove validFile.png?"]
    n12{"Has any in collection"}
    n13["Provide gas cert later"]
    n14(["/landlord/property-details/1#compliance-information"])
    journeyStart -->|"Change"| n0
    n0 -->|"Yes, Continue"| n1
    n0 -->|"No, Continue"| n5
    n1 -->|"Yes, Continue"| n2
    n1 -->|"No, Continue"| n9
    n2 -->|"Continue"| n12
    n12 -->|"No members"| n3
    n2 -->|"Date: 23 October 2027"| n10
    n3 --> n4
    n4 -->|"Continue"| n5
    n4 -->|"Upload another file"| n3
    n4 -->|"Remove validFile.png"| n11
    n5 -->|"Confirm and submit update"| n6
    n1 -.->|"No, Continue"| n7
    n7 -.->|"Continue without a valid gas safety certificate"| n5
    n4 -.->|"Remove validFile.png"| n8
    n9 --> n5
    n10 -->|"Continue without a valid gas safety certificate"| n5
    n10 -->|"Change the issue date"| n2
    n11 -->|"Yes"| n3
    n11 -->|"No / Cancel / Yes"| n4
    n0 -.->|"Provide later (not walked)"| n13
    n13 -.->|"Complete (not walked)"| n5
    n5 -->|"Complete"| n14
    n12 -.->|"Some members (not walked)"| n4
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n6 terminal
    class n7 change
    class n8 change
    class n12 decision
    class n13 gap
    class n14 terminal
```

## Pages

### Does the property have a gas supply or any gas appliances?

`has-gas-supply` · HasGasSupplyStepConfig

[![v1](img/has-gas-supply/v1.thumb.jpg)](img/has-gas-supply/v1.jpg)

Shown on the default route


### Do you have a gas safety certificate for this property?

`has-gas-safety` · HasGasCertStepConfig

[![v1](img/has-gas-safety/v1.thumb.jpg)](img/has-gas-safety/v1.jpg)

Shown on the default route


### What’s the issue date on the gas safety certificate?

`gas-safety-certificate-issue-date` · GasCertIssueDateStepConfig

[![v1](img/gas-safety-certificate-issue-date/v1.thumb.jpg)](img/gas-safety-certificate-issue-date/v1.jpg)

Shown on the default route


### Upload the gas safety certificate

`gas-safety-certificate-file-upload` · UploadGasCertStepConfig

[![v1](img/gas-safety-certificate-file-upload/v1.thumb.jpg)](img/gas-safety-certificate-file-upload/v1.jpg)

Shown on the default route


### You’ve uploaded 1 file

`check-gas-safety-certificate-uploads` · CheckGasCertUploadsStepConfig

[![v1](img/check-gas-safety-certificate-uploads/v1.thumb.jpg)](img/check-gas-safety-certificate-uploads/v1.jpg)

Shown on the default route

[![v2](img/check-gas-safety-certificate-uploads/v2.thumb.jpg)](img/check-gas-safety-certificate-uploads/v2.jpg)

Shown when You’ve uploaded 1 file: Upload another file

[![v3](img/check-gas-safety-certificate-uploads/v3.thumb.jpg)](img/check-gas-safety-certificate-uploads/v3.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png

[![v4](img/check-gas-safety-certificate-uploads/v4.thumb.jpg)](img/check-gas-safety-certificate-uploads/v4.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Upload another file

[![v5](img/check-gas-safety-certificate-uploads/v5.thumb.jpg)](img/check-gas-safety-certificate-uploads/v5.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Remove validFile.png


### Gas safety certificate

`check-gas-safety-answers` · UpdateCheckGasSafetyAnswersStepConfig

[![v1](img/check-gas-safety-answers/v1.thumb.jpg)](img/check-gas-safety-answers/v1.jpg)

Shown on the default route

[![v2](img/check-gas-safety-answers/v2.thumb.jpg)](img/check-gas-safety-answers/v2.jpg)

Shown when Does the property have a gas supply or any gas appliances?: No, Continue

[![v3](img/check-gas-safety-answers/v3.thumb.jpg)](img/check-gas-safety-answers/v3.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Continue

[![v4](img/check-gas-safety-answers/v4.thumb.jpg)](img/check-gas-safety-answers/v4.jpg)

Shown when Starting point: Property is vacant; Do you have a gas safety certificate for this property?: No, Continue


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Does the property have a gas supply or any gas appliances?: No, Continue

[![v3](img/landlord_property-details_1/v3.thumb.jpg)](img/landlord_property-details_1/v3.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Continue

[![v4](img/landlord_property-details_1/v4.thumb.jpg)](img/landlord_property-details_1/v4.jpg)

Shown when What’s the issue date on the gas safety certificate?: Date: 23 October 2027

[![v5](img/landlord_property-details_1/v5.thumb.jpg)](img/landlord_property-details_1/v5.jpg)

Shown when Starting point: Property is vacant

[![v6](img/landlord_property-details_1/v6.thumb.jpg)](img/landlord_property-details_1/v6.jpg)

Shown when Starting point: Property is vacant; Does the property have a gas supply or any gas appliances?: No, Continue

[![v7](img/landlord_property-details_1/v7.thumb.jpg)](img/landlord_property-details_1/v7.jpg)

Shown when Starting point: Property is vacant; Do you have a gas safety certificate for this property?: No, Continue


### You must get a valid gas safety certificate for this property

`gas-safety-certificate-missing` · GasCertMissingStepConfig

[![v1](img/change_gas-safety-certificate-missing/v1.thumb.jpg)](img/change_gas-safety-certificate-missing/v1.jpg)

Shown when Gas safety certificate: Change Does the property have a gas supply or any gas appliances?; Do you have a gas safety certificate for this property?: No, Continue (when changing an answer)


### Are you sure you want to remove validFile.png?

`remove-gas-safety-certificate-upload` · RemoveGasCertUploadStepConfig

[![v1](img/change_remove-gas-safety-certificate-upload/v1.thumb.jpg)](img/change_remove-gas-safety-certificate-upload/v1.jpg)

Shown when Gas safety certificate: Change Does the property have a gas supply or any gas appliances?; You’ve uploaded 1 file: Remove validFile.png (when changing an answer)


### You must get a valid gas safety certificate for this property

`gas-safety-certificate-missing` · GasCertMissingStepConfig

[![v1](img/gas-safety-certificate-missing/v1.thumb.jpg)](img/gas-safety-certificate-missing/v1.jpg)

Shown when Do you have a gas safety certificate for this property?: No, Continue

[![v2](img/gas-safety-certificate-missing/v2.thumb.jpg)](img/gas-safety-certificate-missing/v2.jpg)

Shown when Starting point: Property is vacant; Do you have a gas safety certificate for this property?: No, Continue


### This gas safety certificate has expired

`gas-safety-certificate-expired` · GasCertExpiredStepConfig

[![v1](img/gas-safety-certificate-expired/v1.thumb.jpg)](img/gas-safety-certificate-expired/v1.jpg)

Shown when What’s the issue date on the gas safety certificate?: Date: 23 October 2027


### Are you sure you want to remove validFile.png?

`remove-gas-safety-certificate-upload` · RemoveGasCertUploadStepConfig

[![v1](img/remove-gas-safety-certificate-upload/v1.thumb.jpg)](img/remove-gas-safety-certificate-upload/v1.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png


### Provide gas cert later

`provide-gas-safety-certificate-later` · ProvideGasCertLaterStepConfig

_Not reached by the generator._

### /landlord/property-details/1#compliance-information

`/landlord/property-details/1#compliance-information`

_Not reached by the generator._
