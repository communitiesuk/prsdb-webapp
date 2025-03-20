# ADR-0026: Non-deterministic Journey Support

## Status

Accepted

Date of decision: 20/03/2025

## Context and Problem Statement

Users of the Private Rented Sector Database will need to complete a range of multi-page forms, like landlord registration 
or compliance. We represent multi-page forms as journeys, with each one consisting of an ordered collection of steps. 
At the moment, journeys can only be deterministic, i.e. steps can only be completed in one order (given the user's answers).
However, steps that don't depend on each other could be completed in any order if we supported non-deterministic journeys.

Should we support non-deterministic journeys?

## Considered Options

* Yes, by using DFS to order steps
* No

## Decision Outcome

No, because the product owner has decided that at this time, the benefit of being able to complete multi-page forms in 
different orders isn't worth the cost of the effort it will take to implement. We will re-consider supporting 
non-deterministic journeys once all the journeys have been implemented.

## Pros and Cons of the Options

### Yes, by using DFS to order steps

If we keep track of the steps reachable from each step, we can perform a depth-first search to order them.

* Good, because this will allow us to support non-deterministic journeys.
* Bad, because supporting non-deterministic journeys has proven to be quite complicated, due to uncertainty over the 
variety of edge cases it introduces.

### No

We can enforce an arbitrary ordering on independent steps so journeys remain deterministic.

* Good, because we won't have to put effort into implementing non-deterministic journeys.
* Bad, because non-deterministic journeys will have to be implemented deterministically.
* Neutral, because we can re-visit the idea of implementing non-deterministic journeys later down the line, when we
have more clarity over how we want them to behave.

## More Information

* This [pull request](https://github.com/communitiesuk/prsdb-webapp/pull/281) contains our first attempt at implementing 
non-deterministic journeys.