# Invite a joint landlord journey map report

Generated 2026-11-03T12:31:17.343299800Z at commit `f960f1073`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=true, pdjb-939-property-registration-phase-three=false, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1009-payments=true, pdjb-1210-multi-user-organisations=true, pdjb-1894-gas-certificate-14-month-expiry=false, pdjb-955-associated-parties=false, pdjb-1666-eic-issue-date=false, pdjb-973-asynchronous-email-sending=false.

| Pages | Variants | Edges | Gaps | Errors |
|-------|----------|-------|------|--------|
| 8 | 10 | 13 | 0 | 0 |

## Gaps

None.

## Errors

None.

## Routing code

Every arrow the journey's routing code defines between pages and decisions, for every outcome.

| Arrows | Walked | Not walked | Depend on earlier answers |
|--------|--------|------------|---------------------------|
| 10 | 9 | 1 | 4 |

### Arrows not walked

| From | Outcome | To |
|------|---------|----|
| internal:HasAnyJointLandlordsInvitedStep@task:InviteJointLandlordsTask | SOME_LANDLORDS | check-joint-landlords |

### Arrows that depend on earlier answers

The routing code for these arrows also reads the journey state listed, so with other earlier answers they can lead elsewhere. Where they lead is only known once walked.

| From | Outcome | Walked | Reads |
|------|---------|--------|-------|
| has-joint-landlords | NO | yes | outcome:has-joint-landlords, outcome:start, step:has-joint-landlords |
| check-joint-landlords | COMPLETE | yes | invitedJointLandlordEmails |
| remove-joint-landlord | NO_LANDLORDS | yes | invitedJointLandlordEmails |
| check-invitations | COMPLETE | yes | outcome:has-joint-landlords, outcome:start, step:has-joint-landlords |

## Decisions and the answers they read

| Decision | Steps whose answers it reads | Reads every answer |
|----------|------------------------------|--------------------|
| remove-joint-landlord | invite-another-joint-landlord, invite-joint-landlord | no |
| internal:HasAnyJointLandlordsInvitedStep@task:InviteJointLandlordsTask | invite-another-joint-landlord, invite-joint-landlord, remove-joint-landlord | no |
