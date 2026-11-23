package uk.gov.communities.prsdb.webapp.constants.enums

enum class AddressSearchRegion {
    // Search for English addresses in prop registration also needs them to have a local council.
    // So, right now there is no pure 'English' search.
    ENGLAND_WITH_LOCAL_COUNCIL,
    ENGLAND_AND_WALES,
    UK,
}
