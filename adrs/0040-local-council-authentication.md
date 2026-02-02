# ADR-0040: Local Council Authentication

## Status

Draft

Date of decision: {yyyy-MM-dd}

## Context and Problem Statement

Currently, all user types in the Private Rented Sector Database authenticate via One Login (as per ADR-0007). While
this works well for public-facing users such as landlords and letting agents, it presents a security risk for Local
Council users. Because One Login allows anyone to create an account, we cannot rely on councils' existing
Joiner/Mover/Leaver (JML) processes to manage access to our service. This leaves open the risk that a user might leave
a council but still retain access to our service, potentially allowing them to view or modify sensitive data.

A new Government service called Internal Access has been developed which addresses this concern. Internal Access
restricts account creation to specific email domains and integrates with Google Workspace and Microsoft 365 to enable
Single Sign-On (SSO). This means that when a user loses access to their organizational Google or Microsoft account
(through standard JML processes), they automatically lose access to our service.

However, Internal Access is a new and immature service. Integration with Microsoft 365 is not yet complete, support is
only available 10am-5pm on a best-effort basis, and the service is likely to experience semi-frequent periods of
downtime. While these issues may improve before our service goes live, there is uncertainty around the timeline.

An additional critical constraint is that switching authentication providers after full go-live would be extremely
difficult, bordering on unfeasible. All council users would essentially need to re-register with new credentials,
causing significant disruption. This means our final decision must be implemented before go-live, though changes
during private beta would be more manageable.

Should we continue using One Login for Local Council users, switch to Internal Access, or adopt a hybrid approach?

## Considered Options

* Continue with One Login for Local Council users
* Switch to Internal Access for Local Council users
* Switch to Internal Access but maintain One Login integration as a fallback
* Run Internal Access and One Login in parallel (targeting specific councils)

## Decision Outcome

{Title of Option X}, because {summary justification / rationale}.

## Pros and Cons of the Options

### Continue with One Login for Local Council users

Continue using One Login for Local Council users, implementing the planned changes to use a separate client for this
user group (distinct from public-facing users).

* Good, because One Login is already implemented and well-understood by the team.
* Good, because One Login is a mature, stable service with reliable uptime.
* Good, because it avoids the risk of delays if Internal Access is not ready in time for our go-live date.
* Good, because it requires minimal additional development work (only the separate client configuration).
* Good, because it avoids committing to Internal Access before it has proven itself in production, as switching
  providers post-go-live would be extremely disruptive.
* Bad, because it does not integrate with councils' JML processes, leaving a security risk that former employees may
  retain access to the service.
* Bad, because councils will need to manually manage user access within our application (via invite flows and removal
  processes).

### Switch to Internal Access for Local Council users

Fully switch to Internal Access for Local Council users, removing the One Login integration for this user group.

* Good, because it integrates with councils' existing JML processes via Google Workspace / Microsoft 365,
  automatically revoking access when users leave.
* Good, because it reduces the manual overhead for councils in managing user access (though authorization would still
  be managed within our application).
* Good, because it may enable alternatives to manual invite flows, such as automated join requests based on email
  domain verification.
* Good, because it provides a cleaner architecture with a single authentication provider per user type.
* Bad, because Internal Access is immature and may not be ready in time for our go-live date.
* Bad, because Microsoft 365 integration is not yet complete, excluding councils that use Microsoft (rather than
  Google Workspace).
* Bad, because the service has limited support hours (10am-5pm, best-effort) which may not be sufficient for a
  production service.
* Bad, because semi-frequent downtime could disrupt Local Council users' ability to access our service.
* Bad, because this commits us to Internal Access before it has proven itself reliable, and switching back to One
  Login post-go-live would be extremely disruptive (requiring all users to re-register).

### Switch to Internal Access but maintain One Login integration as a fallback

Switch to Internal Access for Local Council users but keep the One Login integration in place as a dormant fallback,
allowing a quick reversion if Internal Access experiences significant issues.

* Good, because it provides the JML integration benefits of Internal Access when the service is working correctly.
* Good, because it allows a rapid switch back to One Login during private beta if Internal Access proves unsuitable.
* Good, because it reduces risk around our go-live timeline - we can launch with the fallback if needed.
* Bad, because maintaining two authentication integrations increases code complexity and maintenance burden.
* Bad, because the fallback code path may not be well-tested if it's rarely used, potentially causing issues when
  needed.
* Bad, because it still inherits the maturity and reliability risks of Internal Access during normal operation.
* Bad, because switching back to One Login after full go-live would be extremely disruptive (requiring all council
  users to re-register), making the fallback only viable as a pre-go-live safety net rather than a long-term option.

### Run Internal Access and One Login in parallel (targeting specific councils)

Run both Internal Access and One Login simultaneously, routing users to the appropriate authentication provider based
on their council's IT infrastructure (e.g., Internal Access for Google Workspace councils, One Login for others).

* Good, because it maximizes the number of councils that can benefit from JML integration via Internal Access.
* Good, because it provides a gradual migration path as Internal Access matures and adds Microsoft 365 support.
* Good, because it allows targeting Internal Access to specific councils (e.g., those using Google Workspace) during
  private beta phases.
* Good, because authorization is already managed within our application, so handling users from different authentication
  providers is not fundamentally more complex.
* Neutral, because determining which authentication provider to use when a user arrives requires upfront development
  (e.g., users selecting their council, or maintaining email domain mappings), but this is a solvable technical
  challenge.
* Bad, because maintaining two active authentication paths increases code complexity, testing burden, and ongoing
  maintenance overhead.
* Bad, because users may experience different authentication flows depending on their council, which could cause
  confusion and require additional support documentation.

## More Information

* ADR-0007 - User Authentication, Registration, and ID Verification
* ADR-0011 - Access Control
* ADR-0012 - Session Authentication
