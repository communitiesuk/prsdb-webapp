# ADR-0007: User Authentication, Registration, and ID Verification

## Status

Accepted

Date of decision: 2024-08-23

## Context and Problem Statement

The Private Rented Sector Database (PRSDB) will have many different user groups, all of which (except members of the
public) will need to be authenticated to use the service; landlords will also need to have their identities verified.
The types of users and various authentication services are discussed in more detail in [Auth, Registration, and ID
Verification Review.docx](https://mhclg.sharepoint.com/:w:/s/PrivateRentedSector/EQ6sO9czE7FEkk5WyIGPPb8BGbLel_JE1UHoovtJoKI7Wg?e=q2cDQ6).

Which authentication service should we use for which user groups? And how will users therefore register with PRSDB?

## Considered Options

* One Login for all users
* One Login for public users and Entra ID for all others
* Entra ID for MHCLG admins and One Login for all others
* AWS Cognito for all users

## Decision Outcome

Entra ID for MHCLG admins and One Login for all others, because it aligns with government strategy, supports ID
verification for landlords, and simplifies MHCLG admin user management.

### Details
* MHCLG “super admin” users will authenticate with Entra ID, at the “Cl.Cm” level of authentication (i.e. requiring
  2FA). 
  * They will already have Entra ID accounts (provided by MHCLG IT).
  * An initial super admin will need to be manually configured.
  * Subsequent super admin users could then be invited by email (via a link with a short-lived token).
  * The only PRSDB-side registration necessary will be collecting the user’s name.
* Local Authority users will authenticate with One Login, at the “Cl.Cm” level of authentication (i.e. requiring 2FA).
  * They may have to create an account with One Login; they can use their work email addresses if they prefer.
  * An initial admin user for each LA will need to be manually configured.
  * Subsequent LA users could then be invited by email (via a link with a short-lived token).
  * The only PRSDB-side registration necessary will be collecting the user’s name.
* Letting agent users will authenticate with One Login, at the “Cl.Cm” level of authentication (i.e. requiring 2FA).
  * They may have to create an account with One Login; they can use their work email addresses if they prefer.
  * An initial admin user for a letting agency can be created by requesting an email with a registration link (with a
    short-lived token) to be sent to the NTSELAT-registered email address.
  * Subsequent letting agent users could then be invited by email (via a link with a short-lived token).
  * The only PRSDB-side registration necessary will be collecting the user’s name.
* Landlord users will authenticate with One Login, at the “Cl.Cm” level of authentication (i.e. requiring 2FA) plus “P2”
  identity confidence (i.e. medium, requiring identity verification through documentation such as a passport).
  * They may have to create an account with One Login.
  * PRSDB-side registration will involve taking a landlord’s details.
  * If the landlord user is registering a joint or corporate landlord, they can invite additional landlord users by
    email (via a link with a short-lived token).

## Pros and Cons of the Options

### One Login for all users

One Login is the GOV.UK authentication and ID verification service. Per DDaT guidance, it should be used for public
facing services which require users to prove their identity and login.
* Good, because it aligns with government strategy.
* Good, because a single provider is simple.
* Good, because it is a fully managed service.
* Good, because it supports identity verification of landlords.
* Bad, because MHCLG users may expect to use their existing domain accounts and SSO.
* Bad, because the MHCLG PRSDB team would need to carefully manage these very powerful users (e.g. remembering to remove
  accounts in PRSDB when a user leaves MHCLG).

### One Login for public users and Entra ID for all others

MHCLG have an instance of Entra ID hosted on Azure. It allows users to authenticate using their domain accounts. This
option uses One Login for landlords and letting agents but uses Entra ID for LA users and MHCLG super admins.
* Good, because it aligns with government strategy.
* Bad, because multiple providers mean more technical complexity.
* Good, because all providers are fully managed services.
* Good, because it supports identity verification of landlords.
* Good, because MHCLG users can use their familiar accounts and SSO.
* Good, because the existing MHCLG joiners, movers, and leavers processes will ensure super user accounts are disabled
  when a user leaves MHCLG.
* Bad, because LA users would need to be created as guest accounts in Entra ID and this process cannot be fully 
  automated.

### Entra ID for MHCLG admins and One Login for all others

This option uses One Login for landlords, letting agents, and LA users and Entra ID for only MHCLG super users.
* Good, because it aligns with government strategy.
* Bad, because multiple providers mean more technical complexity.
* Good, because all providers are fully managed services.
* Good, because it supports identity verification of landlords.
* Good, because MHCLG users can use their familiar accounts and SSO.
* Good, because the existing MHCLG joiners, movers, and leavers processes will ensure super user accounts are disabled
  when a user leaves MHCLG.

### AWS Cognito for all users

AWS Cognito is the AWS-native identity and access management tool. It could be used to build a fully bespoke
authentication service. (Keycloak is a similar alternative but is not a managed service.)
* Bad, because it does not align with government strategy (because public users are not authenticating with One Login).
* Good, because a single provider is simple.
* Bad, because MHCLG would be responsible for management of the configuration of Cognito (although the technology itself
  is a managed service).
* Bad, because identity verification of landlords would need to be custom built and managed.
* Bad, because MHCLG users may expect to use their existing domain accounts and SSO.
* Bad, because the MHCLG PRSDB team would need to carefully manage these very powerful users (e.g. remembering to remove
  accounts in PRSDB when a user leaves MHCLG).

## More Information

* [Auth, Registration, and ID Verification Review.docx](https://mhclg.sharepoint.com/:w:/s/PrivateRentedSector/EQ6sO9czE7FEkk5WyIGPPb8BGbLel_JE1UHoovtJoKI7Wg?e=q2cDQ6)
* One Login authentication levels: https://docs.sign-in.service.gov.uk/before-integrating/choose-the-level-of-authentication/
* One Login identity verification levels: https://docs.sign-in.service.gov.uk/before-integrating/choose-the-level-of-identity-confidence/ 