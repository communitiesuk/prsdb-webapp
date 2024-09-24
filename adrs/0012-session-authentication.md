# ADR-0012: Session Authentication

## Status

Accepted

Date of decision: 2024-08-23

## Context and Problem Statement

Earlier ADRs have already decided that users with authenticate via external OIDC providers (ADR-0007 - User
Authentication, Registration, and ID Verification). Once a user has authenticated, however, this fact must be persisted
to avoid asking the user to authenticate again on their subsequent requests.

How will we persist a user’s authentication status? I.e. how will we handle sessions?

## Considered Options

* ElastiCache Session Store
* JWTs (With Shared Secret)
* In-Memory Session Store

## Decision Outcome

ElastiCache Session Store, because it is simple, versatile, and scalable.

## Pros and Cons of the Options

### ElastiCache Session Store

Session state is held in a (highly available, persistent) AWS ElastiCache Redis instance, and subsequent requests supply
a session ID (typically via a cookie) so it can be looked up. Authentication and authorisation information (including
e.g. roles) is persisted in the external store.

* Good, because session data survives the service restarting.
* Good, because session data can be shared between multiple parallel instances of the web application.
* Good, because Spring Session can be easily configured to use an external store (e.g. Redis or a JDBC connection).
* Bad, because every request must query the session store, increasing response latency.
* Good, because the session store can be used for things other than storing auth information (e.g. flash messages,
  multi-step process state, data to persist across redirects).
* Good, because request / response sizes are kept small by only storing a session ID in cookies.

### JWTs (With Shared Secret)

JSON Web Tokens, or JWTs, are cryptographically signed packets of data that can be used for securely representing
claims. When the user authenticates, the web application generates a JWT using a secret (a private key) and places it in
a cookie. Future requests pass the cookie back to the application, which can read the claims (i.e. the user roles)
within it and verify the JWT’s authenticity using the same secret. The secret is securely held in AWS Secrets Manager,
with regular automated rotation and deprecation.

* Good, because claims data survives the service restarting.
* Good, because claims data can be shared between multiple parallel instances of the web application (if the secret is
  shared between them).
* Neutral, because integrating JWTs into Spring requires a moderate amount of code, but it is a well-trodden path (with
  many available resources, e.g. this dev.to article).
* Good, because auth of requests takes place without needing to refer to an external service.
* Bad, because session-related data either needs to be stored in an external store, or via relatively complex use of
  other mechanisms such as cookies and query string parameters.
* Bad, because relatively large amounts of data (whole JWTs, any other persisted session data) must be stored in
  cookies, and therefore transmitted with each request.

### In-Memory Session Store

Session state is held in-memory in the web application process. Requests supply a session ID (typically via a cookie).

* Bad, because when the service restarts it loses its sessions (forcing users to log in again).
* Bad, because session data is not shared between parallel instances – so either only one instance must be used, or
  requests from one session must be consistently routed to the same instance (“sticky sessions”), which can lead to
  unbalanced load.
* Good, because Spring Session can easily be configured to use an in-memory store.
* Good, because auth of requests takes place without needing to refer to an external service.
* Good, because other information can also be stored.
* Good, because request / response sizes are kept small by only storing a session ID in cookies.