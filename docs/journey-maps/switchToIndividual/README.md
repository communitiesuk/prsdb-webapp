# Register as the only landlord of a property journey map

Generated 2026-10-23T09:42:28.930976200Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Check these actions before confirming you’re the only landlord"]
    n1["Confirm that you’re the only landlord for this property"]
    n2(["Registered as the only landlord"])
    n3{"Has pending invitations"}
    journeyStart -->|"confirm that you're the only landlord"| n3
    n0 --> n1
    n1 --> n2
    n3 -->|"Yes"| n0
    n3 -->|"No"| n1
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n2 terminal
    class n3 decision
    class n3 entryPoint
```

## Pages

### Check these actions before confirming you’re the only landlord

`check-invitations` · CheckPendingInvitationsStepConfig

[![v1](img/check-invitations/v1.thumb.jpg)](img/check-invitations/v1.jpg)

Shown on the default route


### Confirm that you’re the only landlord for this property

`confirm` · ConfirmOnlyLandlordStepConfig

[![v1](img/confirm/v1.thumb.jpg)](img/confirm/v1.jpg)

Shown on the default route


### Registered as the only landlord

`/landlord/property-details/13/switch-to-individual/confirmation`

[![v1](img/landlord_property-details_13_switch-to-individual_confirmation/v1.thumb.jpg)](img/landlord_property-details_13_switch-to-individual_confirmation/v1.jpg)

Shown on the default route

