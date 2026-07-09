package uk.gov.communities.prsdb.webapp.constants.enums

// Beware reordering the enum values, they are used as the discriminator value for landlords when saved in the database.
// We expect:
// - INDIVIDAL: "0"
// - ORGANISATION: "1"
enum class LandlordType {
    INDIVIDUAL,
    ORGANISATION,
}
