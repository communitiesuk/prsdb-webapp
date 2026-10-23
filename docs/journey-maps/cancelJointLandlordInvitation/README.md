# Cancel a joint landlord invitation journey map

Generated 2026-10-23T09:40:20.247845700Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Do you want to cancel the invitation for pending@example.com?"]
    n1(["Invitation cancelled"])
    n2(["3 Test Lane"])
    n3(["/landlord/property-details/2#landlord-details"])
    journeyStart -->|"Cancel invitation"| n0
    n0 -->|"Yes, cancel this invitation"| n1
    n0 -->|"No, do not cancel this invitation (go back to property record)"| n2
    n0 -->|"Does not want to proceed"| n3
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n1 terminal
    class n2 terminal
    class n3 terminal
```

## Pages

### Do you want to cancel the invitation for pending@example.com?

`are-you-sure` · AreYouSureStepConfig

[![v1](img/are-you-sure/v1.thumb.jpg)](img/are-you-sure/v1.jpg)

Shown on the default route


### Invitation cancelled

`/landlord/cancel-joint-landlord-invitation/confirmation`

[![v1](img/landlord_cancel-joint-landlord-invitation_confirmation/v1.thumb.jpg)](img/landlord_cancel-joint-landlord-invitation_confirmation/v1.jpg)

Shown when Do you want to cancel the invitation for pending@example.com?: Yes, cancel this invitation


### 3 Test Lane

`/landlord/property-details/2`

[![v1](img/landlord_property-details_2/v1.thumb.jpg)](img/landlord_property-details_2/v1.jpg)

Shown when Do you want to cancel the invitation for pending@example.com?: No, do not cancel this invitation (go back to property record)


### /landlord/property-details/2#landlord-details

`/landlord/property-details/2#landlord-details`

_Not reached by the generator._
