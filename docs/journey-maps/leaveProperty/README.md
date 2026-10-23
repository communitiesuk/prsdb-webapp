# Leave a property you jointly own journey map

Generated 2026-10-23T09:42:26.101157800Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

Open `index.html` for the interactive map. `report.md` lists gaps and errors.

```mermaid
flowchart TD
    journeyStart(("Start"))
    n0["Confirm that you’re no longer a landlord for 3 Imaginary Street"]
    n1(["No longer registered as a landlord for 3 Imaginary Street"])
    journeyStart --> n0
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

### Confirm that you’re no longer a landlord for 3 Imaginary Street

`confirm` · ConfirmStepConfig

[![v1](img/confirm/v1.thumb.jpg)](img/confirm/v1.jpg)

Shown on the default route


### No longer registered as a landlord for 3 Imaginary Street

`/landlord/leave-property/2/confirmation`

[![v1](img/landlord_leave-property_2_confirmation/v1.thumb.jpg)](img/landlord_leave-property_2_confirmation/v1.jpg)

Shown on the default route

