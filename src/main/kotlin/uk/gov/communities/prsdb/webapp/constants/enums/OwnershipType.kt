package uk.gov.communities.prsdb.webapp.constants.enums

// Persisted as an ordinal, so new values must only ever be appended.
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

    // Whether the property must have at least one freeholder record, because the ownership type does
    // not already identify all the freeholders. Repeated as ordinals in V1_62_0 and the seed scripts.
    fun requiresFreeholderRecords() =
        this in
            setOf(
                NONE,
                LEASEHOLD_ONLY,
                JOINT_TENANTS,
                TENANTS_IN_COMMON,
                SHARE_OF_JOINT_TENANTS,
                SHARE_OF_TENANTS_IN_COMMON,
            )

    // TODO PDJB-1997: use this to decide whether to show the superior landlords section on the
    //  property record.
    fun requiresSuperiorLandlordInfo() =
        this in
            setOf(
                NONE,
                LEASEHOLD_ONLY,
                SHARE_OF_SOLE_FREEHOLD,
                SHARE_OF_JOINT_TENANTS,
                SHARE_OF_TENANTS_IN_COMMON,
                SHARE_OF_COMMONHOLD,
            )

    // Whether the landlord is themselves one of several freeholders, and so should be added to the
    // freeholders list.
    fun isJointFreeholder() =
        this in
            setOf(
                JOINT_TENANTS,
                TENANTS_IN_COMMON,
                SHARE_OF_JOINT_TENANTS,
                SHARE_OF_TENANTS_IN_COMMON,
            )
}
