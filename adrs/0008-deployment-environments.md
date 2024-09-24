# ADR-0008: Deployment Environments

## Status

Accepted

Date of decision: 2024-08-23

## Context and Problem Statement

To ensure all released code has been tested to a standard with which we are comfortable, we need to define our “route to
live” process.

What environments are necessary? In what order will code changes pass through them? What will occur in each of those
environments, and what implications does that have for how those environments are specified?

## Considered Options

* Local dev and production
* Local dev, test, and production
* Local dev, integration, test, and production
* Local dev, integration, test, NFR, and production

## Decision Outcome

Local dev, integration, test, and production, because each of those environments provides clear benefits.

If cost pressures require test to be non-representative of production, then instead the local dev, integration, test,
NFR, and production option will be chosen, because that reduces costs while still providing the value of each of the
environments.

### Environment Details

#### Local dev

Developers write and run code locally, ideally in the same Docker containers that will ultimately be released to
production. External dependencies (APIs, etc) are mocked out (to keep the local environment responsive, stable, and
predictable).

#### Integration

The integration environment receives updates whenever code changes are pushed to “main”. Any automated end-to-end
tests (that cannot be run locally) are run at that point.

The integration environment is permanently available. It may be run at a reduced scale (compared to production) to
reduce running costs.

#### Test

The test environment receives updates via a manually controlled mechanism. This allows “release candidate” builds to be
tested by stakeholders (without the deployment changing during the testing).

The test environment is ideally permanently available but could be destroyed when not in use to reduce running costs (at
the cost of extra operational complexity).

#### NFR

The NFR environment is only necessary if the test environment is not scaled the same as production. It is created only
as necessary (e.g. when running a load test) and destroyed afterwards.

## Pros and Cons of the Options

### Local dev and production

At its simplest, a route to live needs only two environments: a local environment for developers to write their code,
and a production environment to deploy it to for live use.

* Good, because it is extremely simple (and thus easy to manage).
* Good, because it minimises environments, and therefore cost.
* Bad, because all testing must happen in local development environments, which is very cumbersome (e.g. for acceptance
  testing by stakeholders).

### Local dev, test, and production

The “test” environment is a pre-production environment: similar to production, but only available to the team /
stakeholders. It can be used for automated end-to-end tests and stakeholder acceptance testing.

* Good, because it is relatively simple (and thus easy to manage).
* Good, because cost is relatively low.
* Good, because stakeholders can test in an accessible environment.
* Bad, because either the environment will be rapidly changing (if changes are automatically deployed) or development
  feedback loops will be slow (if changes are withheld / manually released).

### Local dev, integration, test, and production

The “integration” environment is developer- / QA-facing environment that automatically deploys merged code changes. It
can be used for end-to-end tests and internal QA.

* Neutral, because it is somewhat complex (and thus more difficult to manage).
* Bad, because the cost is relatively high.
* Good, because stakeholders can test in a dedicated, stable environment.
* Good, because developers and QAs can get rapid feedback from an environment that can change regularly.

### Local dev, integration, test, NFR, and production

The “NFR” environment is created as a copy of production (i.e. with the same scale and number of resources, and ideally
with a similar scale of data within it). This can be used for load testing (or other non-functional testing) in
situations where the integration and test environments are not representative of production (e.g. they are scaled down
versions, to reduce cost).

* Bad, because it is relatively complex (and thus more difficult to manage – in particular, robust processes for
  ensuring the NFR environment is destroyed as soon as it is no longer needed).
* Neutral, because the cost is somewhat high.
* Good because stakeholders can test in a dedicated, stable environment.
* Good, because developers and QAs can get rapid feedback from an environment that can change regularly. 