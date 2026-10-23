# Invite a joint landlord journey map

Generated 2026-10-23T09:36:39.865524100Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Invite joint landlords"]
    n1(["1 Example Road EG1 1AA"])
    n2["Invite a joint landlord to this property"]
    n3["You’ve added 1 joint landlord (2 variants)"]
    n4["Check your answers"]
    n5(["Joint landlord invitations sent"])
    n6["Invite a joint landlord to this property"]
    n7["Are you sure you want to remove jane.smith@example.com? (2 variants)"]
    n8{"Has any joint landlords invited"}
    journeyStart -->|"invite them to join the property"| n0
    n0 -->|"Yes"| n8
    n8 -->|"No landlords"| n2
    n0 -->|"No, I am the only landlord for this property"| n1
    n2 --> n3
    n3 -->|"Save and continue"| n4
    n3 -->|"Add another landlord / Change Joint landlord 1 / Change Joint landlord 2"| n6
    n3 -->|"Remove Joint landlord 1 / Remove Joint landlord 2"| n7
    n4 -->|"Confirm and send"| n5
    n4 -->|"Change Invitations"| n3
    n6 --> n3
    n7 -->|"Yes"| n0
    n7 -->|"No / Cancel / Yes"| n3
    n8 -.->|"Some landlords (not walked)"| n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n1 terminal
    class n5 terminal
    class n8 decision
```

## Pages

### Invite joint landlords

`has-joint-landlords` · HasJointLandlordsConfig

[![v1](img/has-joint-landlords/v1.thumb.jpg)](img/has-joint-landlords/v1.jpg)

Shown on the default route


### 1 Example Road EG1 1AA

`/landlord/property-details/1`

[![v1](img/landlord_property-details_1/v1.thumb.jpg)](img/landlord_property-details_1/v1.jpg)

Shown when Invite joint landlords: No, I am the only landlord for this property


### Invite a joint landlord to this property

`invite-joint-landlord` · InviteJointLandlordStepConfig

[![v1](img/invite-joint-landlord/v1.thumb.jpg)](img/invite-joint-landlord/v1.jpg)

Shown on the default route


### You’ve added 1 joint landlord

`check-joint-landlords` · CheckJointLandlordsStepConfig

[![v1](img/check-joint-landlords/v1.thumb.jpg)](img/check-joint-landlords/v1.jpg)

Shown on the default route

[![v2](img/check-joint-landlords/v2.thumb.jpg)](img/check-joint-landlords/v2.jpg)

Shown when You’ve added 1 joint landlord: Add another landlord


### Check your answers

`check-invitations` · CheckInvitationsStepConfig

[![v1](img/check-invitations/v1.thumb.jpg)](img/check-invitations/v1.jpg)

Shown on the default route


### Joint landlord invitations sent

`/landlord/property-details/1/invite-joint-landlord/confirmation`

[![v1](img/landlord_property-details_1_invite-joint-landlord_confirmation/v1.thumb.jpg)](img/landlord_property-details_1_invite-joint-landlord_confirmation/v1.jpg)

Shown when Check your answers: Confirm and send


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

