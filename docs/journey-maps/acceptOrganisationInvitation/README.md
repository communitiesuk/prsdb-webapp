# Join an organisation journey map

Generated 2026-10-23T09:42:37.413333100Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=true.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["You need to use a different GOV.UK One Login to accept this invitation"]
    n1["Join your organisation"]
    n2["What is your full name?"]
    n3["What is your email address?"]
    n4["Check your answers"]
    n5(["Invitation accepted"])
    n6["There was a problem with this invitation link"]
    n7{"Validate token"}
    n8{"Check user is landlord"}
    journeyStart -->|"Link in the invitation email"| n7
    n1 --> n2
    n2 --> n3
    n3 --> n4
    n4 --> n5
    n7 -->|"Valid"| n8
    n7 -->|"Invalid"| n6
    n8 -->|"Already a landlord"| n0
    n8 -->|"Not a landlord"| n1
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n5 terminal
    class n7 decision
    class n7 entryPoint
    class n8 decision
```

## Pages

### You need to use a different GOV.UK One Login to accept this invitation

`already-landlord` · UserIsAlreadyLandlordStepConfig

[![v1](img/already-landlord/v1.thumb.jpg)](img/already-landlord/v1.jpg)

Shown on the default route


### Join your organisation

`join-your-organisation` · JoinOrganisationStepConfig

[![v1](img/join-your-organisation/v1.thumb.jpg)](img/join-your-organisation/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your full name?

`name` · FullNameStepConfig

[![v1](img/name/v1.thumb.jpg)](img/name/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### What is your email address?

`email` · EmailAddressStepConfig

[![v1](img/email/v1.thumb.jpg)](img/email/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Check your answers

`check-answers` · AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig

[![v1](img/check-answers/v1.thumb.jpg)](img/check-answers/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### Invitation accepted

`/landlord/accept-invitation/confirmation`

[![v1](img/landlord_accept-invitation_confirmation/v1.thumb.jpg)](img/landlord_accept-invitation_confirmation/v1.jpg)

Shown when Starting point: Invited user is not a landlord


### There was a problem with this invitation link

`invalid-link` · InvalidLinkStepConfig

[![v1](img/invalid-link/v1.thumb.jpg)](img/invalid-link/v1.jpg)

Shown when Starting point: Invitation has expired

