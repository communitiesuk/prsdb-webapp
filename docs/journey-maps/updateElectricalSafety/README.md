# Update electrical safety journey map

Generated 2026-10-23T09:34:19.894367100Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    subgraph c1["Changing answers"]
        n6["What’s the expiry date on the Electrical Installation Condition Report?"]
        n7["You must get a valid electrical safety certificate for this property"]
        n8["Are you sure you want to remove validFile.png?"]
        n9["You’ve uploaded 2 files"]
    end
    n0["Which electrical safety certificate do you have for this property?"]
    n1["What’s the expiry date on the Electrical Installation Certificate? (2 variants)"]
    n2["Upload the Electrical Installation Certificate (EIC) (2 variants)"]
    n3["You’ve uploaded 1 file (6 variants)"]
    n4["Electrical safety certificate (3 variants)"]
    n5(["1 Example Road EG1 1AA (5 variants)"])
    n10["You must get a valid electrical safety certificate for this property (2 variants)"]
    n11["This electrical safety certificate has expired"]
    n12["Are you sure you want to remove validFile.png?"]
    n13{"Has any in collection"}
    n14["Provide electrical cert later"]
    n15(["/landlord/property-details/1#compliance-information"])
    journeyStart -->|"Change"| n0
    n0 -->|"Electrical Installation Certificate (EIC), Continue / Electrical Installation Condition Report (EICR), Continue"| n1
    n0 -->|"None of the above, Continue"| n10
    n1 -->|"Continue"| n13
    n13 -->|"No members"| n2
    n1 -->|"Date: 23 September 2026"| n11
    n2 --> n3
    n3 -->|"Continue"| n4
    n3 -->|"Upload another file"| n2
    n3 -->|"Remove validFile.png"| n12
    n4 -->|"Confirm and submit update"| n5
    n0 -.->|"Electrical Installation Condition Report (EICR), Continue"| n6
    n0 -.->|"None of the above, Continue"| n7
    n7 -.->|"Continue without a valid electrical safety certificate"| n4
    n3 -.->|"Remove validFile.png"| n8
    n2 -.->|"Upload a file"| n9
    n8 -.->|"Yes"| n2
    n8 -.->|"No / Cancel"| n3
    n10 --> n4
    n11 -->|"Continue without a valid electrical safety certificate"| n4
    n11 -->|"Change the expiry date"| n1
    n12 -->|"Yes"| n2
    n12 -->|"No / Cancel / Yes"| n3
    n0 -.->|"Provide this later (not walked)"| n14
    n14 -.->|"Complete (not walked)"| n4
    n4 -->|"Complete"| n15
    n13 -.->|"Some members (not walked)"| n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n5 terminal
    class n6 change
    class n7 change
    class n8 change
    class n9 change
    class n13 decision
    class n14 gap
    class n15 terminal
```

## Pages

### Which electrical safety certificate do you have for this property?

`has-electrical-safety` · HasElectricalCertStepConfig

[![v1](img/has-electrical-safety/v1.thumb.jpg)](img/has-electrical-safety/v1.jpg)

Shown on the default route


### What’s the expiry date on the Electrical Installation Certificate?

`electrical-safety-certificate-expiry-date` · ElectricalCertExpiryDateStepConfig

[![v1](img/electrical-safety-certificate-expiry-date/v1.thumb.jpg)](img/electrical-safety-certificate-expiry-date/v1.jpg)

Shown on the default route

[![v2](img/electrical-safety-certificate-expiry-date/v2.thumb.jpg)](img/electrical-safety-certificate-expiry-date/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Continue


### Upload the Electrical Installation Certificate (EIC)

`electrical-safety-certificate-file-upload` · UploadElectricalCertStepConfig

[![v1](img/electrical-safety-certificate-file-upload/v1.thumb.jpg)](img/electrical-safety-certificate-file-upload/v1.jpg)

Shown on the default route

[![v2](img/electrical-safety-certificate-file-upload/v2.thumb.jpg)](img/electrical-safety-certificate-file-upload/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Continue


### You’ve uploaded 1 file

`check-electrical-safety-certificate-uploads` · CheckElectricalCertUploadsStepConfig

[![v1](img/check-electrical-safety-certificate-uploads/v1.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v1.jpg)

Shown on the default route

[![v2](img/check-electrical-safety-certificate-uploads/v2.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v2.jpg)

Shown when Electrical safety certificate: Change Which electrical safety certificate do you have for this property?; Are you sure you want to remove validFile.png?: No (when changing an answer)

[![v3](img/check-electrical-safety-certificate-uploads/v3.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v3.jpg)

Shown when You’ve uploaded 1 file: Upload another file

[![v4](img/check-electrical-safety-certificate-uploads/v4.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v4.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Upload another file

[![v5](img/check-electrical-safety-certificate-uploads/v5.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v5.jpg)

Shown when You’ve uploaded 1 file: Upload another file; You’ve uploaded 2 files: Remove validFile.png

[![v6](img/check-electrical-safety-certificate-uploads/v6.thumb.jpg)](img/check-electrical-safety-certificate-uploads/v6.jpg)

Shown when Starting point: Property is vacant; You’ve uploaded 1 file: Upload another file


### Electrical safety certificate

`check-electrical-safety-answers` · UpdateCheckElectricalSafetyAnswersStepConfig

[![v1](img/check-electrical-safety-answers/v1.thumb.jpg)](img/check-electrical-safety-answers/v1.jpg)

Shown on the default route

[![v2](img/check-electrical-safety-answers/v2.thumb.jpg)](img/check-electrical-safety-answers/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: None of the above, Continue

[![v3](img/check-electrical-safety-answers/v3.thumb.jpg)](img/check-electrical-safety-answers/v3.jpg)

Shown when Starting point: Property is vacant; Which electrical safety certificate do you have for this property?: None of the above, Continue


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown on the default route

[![v2](img/landlord_property-details_1/v2.thumb.jpg)](img/landlord_property-details_1/v2.jpg)

Shown when Which electrical safety certificate do you have for this property?: None of the above, Continue

[![v3](img/landlord_property-details_1/v3.thumb.jpg)](img/landlord_property-details_1/v3.jpg)

Shown when What’s the expiry date on the Electrical Installation Certificate?: Date: 23 September 2026

[![v4](img/landlord_property-details_1/v4.thumb.jpg)](img/landlord_property-details_1/v4.jpg)

Shown when Starting point: Property is vacant

[![v5](img/landlord_property-details_1/v5.thumb.jpg)](img/landlord_property-details_1/v5.jpg)

Shown when Starting point: Property is vacant; Which electrical safety certificate do you have for this property?: None of the above, Continue


### What’s the expiry date on the Electrical Installation Condition Report?

`electrical-safety-certificate-expiry-date` · ElectricalCertExpiryDateStepConfig

[![v1](img/change_electrical-safety-certificate-expiry-date/v1.thumb.jpg)](img/change_electrical-safety-certificate-expiry-date/v1.jpg)

Shown when Electrical safety certificate: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: Electrical Installation Condition Report (EICR), Continue (when changing an answer)


### You must get a valid electrical safety certificate for this property

`electrical-safety-certificate-missing` · ElectricalCertMissingStepConfig

[![v1](img/change_electrical-safety-certificate-missing/v1.thumb.jpg)](img/change_electrical-safety-certificate-missing/v1.jpg)

Shown when Electrical safety certificate: Change Which electrical safety certificate do you have for this property?; Which electrical safety certificate do you have for this property?: None of the above, Continue (when changing an answer)


### Are you sure you want to remove validFile.png?

`remove-electrical-safety-certificate-upload` · RemoveElectricalCertUploadStepConfig

[![v1](img/change_remove-electrical-safety-certificate-upload/v1.thumb.jpg)](img/change_remove-electrical-safety-certificate-upload/v1.jpg)

Shown when Electrical safety certificate: Change Which electrical safety certificate do you have for this property?; You’ve uploaded 1 file: Remove validFile.png (when changing an answer)


### You’ve uploaded 2 files

`check-electrical-safety-certificate-uploads` · CheckElectricalCertUploadsStepConfig

[![v1](img/change_check-electrical-safety-certificate-uploads/v1.thumb.jpg)](img/change_check-electrical-safety-certificate-uploads/v1.jpg)

Shown when Electrical safety certificate: Change Which electrical safety certificate do you have for this property? (when changing an answer)


### You must get a valid electrical safety certificate for this property

`electrical-safety-certificate-missing` · ElectricalCertMissingStepConfig

[![v1](img/electrical-safety-certificate-missing/v1.thumb.jpg)](img/electrical-safety-certificate-missing/v1.jpg)

Shown when Which electrical safety certificate do you have for this property?: None of the above, Continue

[![v2](img/electrical-safety-certificate-missing/v2.thumb.jpg)](img/electrical-safety-certificate-missing/v2.jpg)

Shown when Starting point: Property is vacant; Which electrical safety certificate do you have for this property?: None of the above, Continue


### This electrical safety certificate has expired

`electrical-safety-certificate-expired` · ElectricalCertExpiredStepConfig

[![v1](img/electrical-safety-certificate-expired/v1.thumb.jpg)](img/electrical-safety-certificate-expired/v1.jpg)

Shown when What’s the expiry date on the Electrical Installation Certificate?: Date: 23 September 2026


### Are you sure you want to remove validFile.png?

`remove-electrical-safety-certificate-upload` · RemoveElectricalCertUploadStepConfig

[![v1](img/remove-electrical-safety-certificate-upload/v1.thumb.jpg)](img/remove-electrical-safety-certificate-upload/v1.jpg)

Shown when You’ve uploaded 1 file: Remove validFile.png


### Provide electrical cert later

`provide-electrical-safety-certificate-later` · ProvideElectricalCertLaterStepConfig

_Not reached by the generator._

### /landlord/property-details/1#compliance-information

`/landlord/property-details/1#compliance-information`

_Not reached by the generator._
