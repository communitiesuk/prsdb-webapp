# ADR-0035: Performance testing scope

## Status

Draft

Date of decision: TBC {yyyy-MM-dd}

## Context and Problem Statement

What types of performance tests are in scope for our performance testing e.g. stress tests, spike tests etc

## Considered Options

* Full suite of tests: performance, load, spike, soak, stress
* Full suite minus stress tests
* Performance tests, load, and spike tests
* Performance tests and load tests only

## Decision Outcome

{Title of Option X}, because {summary justification / rationale}.

## Pros and Cons of the Options

### Full suite of  tests

The most comprehensive suite of tests, covering everything up to and including pushing the system to breaking point in order to
know exactly how much performance headroom we have and what impact changes have on that headroom. Specially would include:

- performance tests - testing individual actions/requests are performant independent of the wider load on the system
- load tests - simulating our predicted 'normal' usage of the system, and ensuring actions/requests are performant under that load
- stress tests - simulating our predicted
  average use tests, stress tests, spike tests, soak tests, and breaking point tests.

* Good, because it gives us a baseline of performance for each type of action.
* Good, because it benchmarks the performance we'd expect to see during normal usage to check that it's acceptable.
* Good, because it tests that the service can handle sustained high loads.
* Good, because it tests that the service can withstand sudden spikes in load followed by rapid ramp-downs as might happen on registration
  deadlines.
* Good, because it tests that the endurance of the service over a long period of time.
* Good, because it lets us under what load the service would fall over or performance would degrade, letting us calculate our headroom.
* Good, because it would allow us to run different types of tests at different frequencies or on different triggers.
* Bad, because the full suite would take a long time to run, making it less likely we'd do so.
* Bad, because it would be expensive to run the full suite of tests, particularly the breaking point tests.
* Bad, because we would likely need to give advanced notice to MHCLG (and maybe AWS) before running the breaking point tests.

### Full suite minus breaking point tests

As above,

* {Good | Bad | Neutral}, because {argument a}.
* {Good | Bad | Neutral}, because {argument b}.
* {... etc ...}

### Smoke tests, average use tests, and spike tests

{Description of the option}

* {Good | Bad | Neutral}, because {argument a}.
* {Good | Bad | Neutral}, because {argument b}.
* {... etc ...}

### Smoke tests and average use tests only

{Description of the option}

* {Good | Bad | Neutral}, because {argument a}.
* {Good | Bad | Neutral}, because {argument b}.
* {... etc ...}



