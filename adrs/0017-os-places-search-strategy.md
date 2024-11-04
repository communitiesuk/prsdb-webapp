# ADR-0017: OS Places Search Strategy

## Status

Accepted

Date of decision: 2024-10-31

## Context and Problem Statement

Users of the Private Rented Sector Database will need to enter addresses: landlord contact addresses and property
addresses, in particular. In aid of this, users will be able to search for UK addresses through entering a building
name/number and postcode. We are using OS Places to perform these lookups.

Which OS Places endpoint should we use?

## Considered Options

* `/find`
* `/postcode`

## Decision Outcome

`/find`, because the data returned will not require any extra processing.

## Pros and Cons of the Options

### `/find`

The `/find` endpoint performs a fuzzy match against a freetext query.

* Good, because results can be limited to addresses above a given match score.
* Bad, because results can include addresses with postcodes similar to (but not exactly) the given postcode.
* Good, because the first result will have the highest match score and therefore is likely to be the one the user is
  looking for.

### `/postcode`

The `postcode` endpoint returns the addresses that correspond with a postcode.

* Good, because results will only include addresses of the given postcode.
* Bad, because we will be responsible for filtering the data to only include addresses with the given building
  name/number.

## More Information

* [OS Places Specification](https://osdatahub.os.uk/docs/places/technicalSpecification)