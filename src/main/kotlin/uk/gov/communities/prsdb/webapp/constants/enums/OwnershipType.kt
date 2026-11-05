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
    // TODO PDJB-2015: make OwnershipStructureRoutingStepConfig.asksFreeholders delegate to this.
    //  The enabling step is the journey answers -> OwnershipType mapping on OwnershipTypeState.
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
}
