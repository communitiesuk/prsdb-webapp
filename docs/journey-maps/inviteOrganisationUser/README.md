# Invite a team member journey map

Generated 2026-11-03T12:37:23.085226Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Invite a team member"]
    n1(["Team members"])
    journeyStart -->|"Invite a team member"| n0
    n0 --> n1
    classDef gap fill:#f3f2f1,stroke:#b1b4b6,stroke-dasharray: 5 5,color:#505a5f
    classDef decision fill:#fff7bf,stroke:#b58840
    classDef terminal fill:#cce2d8,stroke:#00703c
    classDef change stroke:#1d70b8,stroke-width:3px
    classDef startNode fill:#00703c,stroke:#00703c,color:#ffffff
    classDef entryPoint stroke:#00703c,stroke-width:4px
    class journeyStart startNode
    class n0 entryPoint
    class n1 terminal
```

## Pages

### Invite a team member

`invite-team-member` · InviteOrganisationalLandlordUserStepConfig

[![v1](img/invite-team-member/v1.thumb.jpg)](img/invite-team-member/v1.jpg)

Shown on the default route


### Team members

`/landlord/team-members`

[![v1](img/landlord_team-members/v1.thumb.jpg)](img/landlord_team-members/v1.jpg)

Shown when Invite a team member: Administrator

