package uk.gov.communities.prsdb.webapp.constants.enums

// Persisted as an ordinal in property_ownership.ownership_type. The declaration order is
// load-bearing: new values must only ever be appended.
enum class OwnershipType {
    // Does not own the property
    NONE,

    // Selected "Leasehold" only
    LEASEHOLD_ONLY,

    // Selected "Freehold" only
    SOLE_FREEHOLD,
    JOINT_TENANTS,
    TENANTS_IN_COMMON,
    COMMONHOLD,

    // Selected both "Leasehold" and "Freehold"
    SHARE_OF_SOLE_FREEHOLD,
    SHARE_OF_JOINT_TENANTS,
    SHARE_OF_TENANTS_IN_COMMON,
    SHARE_OF_COMMONHOLD,
    ;

    // Whether the property has a freeholder other than the landlord themselves, in which case it
    // must have at least one freeholder record. The same rule is repeated as a list of ordinals in
    // V1_61_0__add_ownership_type_and_associated_party_tables.sql and in the seed scripts, which
    // cannot call into Kotlin.
    fun impliesAnotherFreeholder() =
        this in
            setOf(
                NONE,
                LEASEHOLD_ONLY,
                JOINT_TENANTS,
                TENANTS_IN_COMMON,
                SHARE_OF_JOINT_TENANTS,
                SHARE_OF_TENANTS_IN_COMMON,
            )
}
