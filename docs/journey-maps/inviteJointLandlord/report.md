# Invite a joint landlord journey map report

Generated 2026-10-23T09:36:39.865524100Z at commit `3a5832daa`. Feature flags: failover-test-endpoints=false, subject-identifier-page=true, pdjb-939-property-registration-phase-two=false, pdjb-1053-dashboard-nav-link=true, pdjb-1022-delegate-to-letting-agent=true, pdjb-1664-password-benchmark-endpoint=false, pdjb-238-gatling-post-endpoint=false, pdjb-1040-correspondence-address=true, pdjb-1009-payments=false, pdjb-926-landlord-dashboard-update=true, pdjb-1210-multi-user-organisations=false.

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
