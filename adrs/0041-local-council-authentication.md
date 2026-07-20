# ADR-0041: Local Council Authentication

## Status

Accepted

Date of decision: 2026-07-09

## Context and Problem Statement

We will need to authenticate large volumes of local council users. What mechanism should we use to do so?

## Considered Options

* Internal Access
* MHCLG Entra
* One Login + Email Verification

## Decision Outcome

Internal Access, because it lets users auth with familiar accounts, leverages existing JML processes, and 
offers at least critical support whilst it is in "maintenance mode" as a project. If its status changes, or
it proves too unstable, we can revisit our decision; we have time to do so, as local council users are not
expected in public beta until March 2027.

## Pros and Cons of the Options

### Internal Access

Internal Access, aka Public Sector Sign In, is a GDS service that federates authentication for public
sector organisations (using Microsoft Entra or Google Workspace). This allows users to sign in with their
existing work accounts. Internal Access is currently in private beta, and as of early July 2026, has been
placed in "maintenance mode" - offering critical support only, and onboarding no new services.

* Good, because Internal Access is MHCLG's standing recommendation for this use case
* Good, because using existing work accounts mean access will be revoked if that account is disabled
* Good, because users are typically comfortable with using their work accounts to log in to services
* Good, because it is flexible enough to cope with either Microsoft (the dominant provider) or Google
  (which some councils may use)
* Good, because operational burdens and dependencies are low
* Bad, because its future is uncertain - it's not clear when it will move out of "maintenance mode" or
  introduce important features (such as true Microsoft SSO, rather than email verification)

### MHCLG Entra

MHCLG maintain their own Entra instance, which is where all communities.gov.uk identities are provisioned and managed.
Entra allows guest identities, which can be configured to federate authentication to a 'home' Entra, which should mean
local council users could be set up to log in with their existing accounts (and password, MFA, etc).

* Good, because using existing work accounts mean access will be revoked if that account is disabled
* Good, because users are typically comfortable with using their work accounts to log in to services
* Neutral, because it requires councils to be using Entra, but we believe this to be very commonplace
* Bad, because it places an operational dependency on another team within MHCLG
* Bad, because it places an operational burden on the Local Transcribe team to ensure secrets are regularly rotated

### One Login + email verification

One Login is central government's flagship authentication product, allowing users to create a single account they
can reuse across multiple government services. It is primarily designed for members of the public, however, rather
than local government users. To establish that a One Login user still has access to a local council account, we
would need to send a verification email.

* Good, because it is a proven, mature service, already in use in PRSDB
* Good, because operational burdens and dependencies are low
* Neutral, because we can establish the user has an active local council account via email verification, but this requires
  extra work and is a frustrating UX
* Bad, because users have to use a non-work account, which may be confusing or unwanted