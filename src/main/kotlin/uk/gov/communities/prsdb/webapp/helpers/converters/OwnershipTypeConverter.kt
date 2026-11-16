package uk.gov.communities.prsdb.webapp.helpers.converters

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955

// Single source of truth for the PDJB-955 ownership type mapping, duplicated as a SQL CASE in
// V1_62_0 and the seed scripts; OwnershipTypeConverterTests pins the ordinals.
// TODO PDJB-2019: Delete this once the legacy enum and column are gone.
object OwnershipTypeConverter {
    fun convert(ownershipType: OwnershipTypeBeforePdjb955): OwnershipType =
        when (ownershipType) {
            OwnershipTypeBeforePdjb955.FREEHOLD -> OwnershipType.SOLE_FREEHOLD
            OwnershipTypeBeforePdjb955.LEASEHOLD -> OwnershipType.LEASEHOLD_ONLY
            OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD -> OwnershipType.JOINT_TENANTS
            OwnershipTypeBeforePdjb955.COMMONHOLD -> OwnershipType.COMMONHOLD
        }

    // Lossy (ten values to four), but every legacy value round-trips exactly, which is what keeps the
    // flag-off path's behaviour unchanged. Exhaustive with no else branch so that a new OwnershipType
    // value fails to compile rather than failing at runtime against a NOT NULL column.
    fun convertToBeforePdjb955(ownershipType: OwnershipType): OwnershipTypeBeforePdjb955 =
        when (ownershipType) {
            OwnershipType.NONE -> OwnershipTypeBeforePdjb955.LEASEHOLD
            OwnershipType.LEASEHOLD_ONLY -> OwnershipTypeBeforePdjb955.LEASEHOLD
            OwnershipType.SOLE_FREEHOLD -> OwnershipTypeBeforePdjb955.FREEHOLD
            OwnershipType.JOINT_TENANTS -> OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD
            OwnershipType.TENANTS_IN_COMMON -> OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD
            OwnershipType.COMMONHOLD -> OwnershipTypeBeforePdjb955.COMMONHOLD
            OwnershipType.SHARE_OF_SOLE_FREEHOLD -> OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD
            OwnershipType.SHARE_OF_JOINT_TENANTS -> OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD
            OwnershipType.SHARE_OF_TENANTS_IN_COMMON -> OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD
            OwnershipType.SHARE_OF_COMMONHOLD -> OwnershipTypeBeforePdjb955.COMMONHOLD
        }
}
