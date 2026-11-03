# Accept an invitation to act as a letting agent journey map report

Generated 2026-11-03T12:37:34.521369600Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 4 | 4 | 6 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 5 | 5 | 0 | 3 |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| set-password | COMPLETE | yes | hasExistingPassword, invitationToken, outcome:internal:HasPasswordStep, outcome:start |
| enter-password | COMPLETE | yes | hasExistingPassword, invitationToken, outcome:internal:HasPasswordStep, outcome:start |
| confirmation | COMPLETE | yes | invitationToken |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| internal:HasPasswordStep | start | no |
