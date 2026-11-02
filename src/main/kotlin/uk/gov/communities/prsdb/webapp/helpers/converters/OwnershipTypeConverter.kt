package uk.gov.communities.prsdb.webapp.helpers.converters

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955

// Single source of truth for the PDJB-955 ownership type mapping, duplicated as a SQL CASE in
// V1_61_0 and the seed scripts; OwnershipTypeConverterTests pins the ordinals.
// TODO PDJB-2019: Delete this once the legacy enum and column are gone.
object OwnershipTypeConverter {
    fun convert(ownershipType: OwnershipTypeBeforePdjb955): OwnershipType =
        when (ownershipType) {
            OwnershipTypeBeforePdjb955.FREEHOLD -> OwnershipType.SOLE_FREEHOLD
            OwnershipTypeBeforePdjb955.LEASEHOLD -> OwnershipType.LEASEHOLD_ONLY
            OwnershipTypeBeforePdjb955.SHARE_OF_FREEHOLD -> OwnershipType.JOINT_TENANTS
            OwnershipTypeBeforePdjb955.COMMONHOLD -> OwnershipType.COMMONHOLD
        }
}
