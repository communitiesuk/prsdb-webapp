# Change a team member's access level journey map

Generated 2026-10-23T09:42:33.205948300Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=true.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Change access level"]
    n1(["Team members (3 variants)"])
    n2["Confirm removal - continuing will remove this user from the organisation (TODO PDJB-1764: page content)"]
    journeyStart -->|"Change Editor 1"| n0
    n0 -->|"Administrator / Editor"| n1
    n0 -->|"Remove access"| n2
    n2 --> n1
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

### Change access level

`access-level` · AccessLevelStepConfig

[![v1](img/access-level/v1.thumb.jpg)](img/access-level/v1.jpg)

Shown on the default route


### Team members

`/landlord/team-members`

[![v1](img/landlord_team-members/v1.thumb.jpg)](img/landlord_team-members/v1.jpg)

Shown when Change access level: Administrator

[![v2](img/landlord_team-members/v2.thumb.jpg)](img/landlord_team-members/v2.jpg)

Shown when Change access level: Editor

[![v3](img/landlord_team-members/v3.thumb.jpg)](img/landlord_team-members/v3.jpg)

Shown when Change access level: Remove access


### Confirm removal - continuing will remove this user from the organisation (TODO PDJB-1764: page content)

`confirm-removal` · RemoveOrgUserConfirmationStepConfig

[![v1](img/confirm-removal/v1.thumb.jpg)](img/confirm-removal/v1.jpg)

Shown when Change access level: Remove access

