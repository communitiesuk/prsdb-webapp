package uk.gov.communities.prsdb.webapp.constants.enums

// TODO PDJB-2019: Delete this enum and the ownership_type_before_pdjb955 column once
//  PDJB-2016 has removed the pdjb-955-associated-parties flag and the flag-off code paths.
enum class OwnershipTypeBeforePdjb955 {
    FREEHOLD,
    LEASEHOLD,
    SHARE_OF_FREEHOLD,
    COMMONHOLD,
}
