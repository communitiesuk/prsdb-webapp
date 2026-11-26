package uk.gov.communities.prsdb.webapp.constants.enums

enum class AddressSearchRegion {
    // Search for English addresses in prop registration also needs them to have a local council.
    // So, right now there is no pure 'English' search.
    WITH_ENGLISH_LOCAL_COUNCIL,
    ENGLAND_AND_WALES,

    // This means all addresses as we only load UK ones
    UK,
}
