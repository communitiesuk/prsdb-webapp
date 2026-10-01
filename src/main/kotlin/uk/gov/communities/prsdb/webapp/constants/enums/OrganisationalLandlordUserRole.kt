package uk.gov.communities.prsdb.webapp.constants.enums

// Beware reordering the enum values, the ordinal value for the organisational landlord user role is saved to the database.
// We expect:
// - ADMIN: "0"
// - EDITOR: "1"
enum class OrganisationalLandlordUserRole {
    ADMIN,
    EDITOR,
}
