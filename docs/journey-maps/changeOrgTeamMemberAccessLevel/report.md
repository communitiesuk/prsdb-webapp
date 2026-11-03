# Change a team member's access level journey map report

Generated 2026-11-03T12:37:25.754712200Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 3 | 5 | 3 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 2 | 2 | 0 | 2 |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| access-level | COMPLETE | yes | otherOrganisationalLandlordUserId, step:access-level |
| confirm-removal | COMPLETE | yes | otherOrganisationalLandlordUserId, removedMemberName |
