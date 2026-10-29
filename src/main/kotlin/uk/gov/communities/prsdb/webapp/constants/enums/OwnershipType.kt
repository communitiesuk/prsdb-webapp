package uk.gov.communities.prsdb.webapp.constants.enums

// Persisted as an ordinal in property_ownership.ownership_type. The declaration order is
// load-bearing: new values must only ever be appended.
enum class OwnershipType {
    // Does not own the property
    NONE,

    // Selected "Leasehold" only
    LEASEHOLD_ONLY,

    // Selected "Freehold" only
    SOLE_OWNERSHIP,
    JOINT_TENANTS,
    TENANTS_IN_COMMON,
    COMMONHOLD,

    // Selected both "Leasehold" and "Freehold"
    SHARE_OF_SOLE_FREEHOLD,
    SHARE_OF_JOINT_TENANTS,
    SHARE_OF_TENANTS_IN_COMMON,
    SHARE_OF_COMMONHOLD,
}
