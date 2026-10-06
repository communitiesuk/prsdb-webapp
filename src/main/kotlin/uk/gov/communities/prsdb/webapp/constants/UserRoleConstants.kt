package uk.gov.communities.prsdb.webapp.constants

const val ROLE_INDIVIDUAL_LANDLORD = "ROLE_INDIVIDUAL_LANDLORD"

const val ROLE_ORG_ADMIN = "ROLE_ORG_ADMIN"

const val ROLE_ORG_EDITOR = "ROLE_ORG_EDITOR"

const val ROLE_LOCAL_COUNCIL_ADMIN = "ROLE_LOCAL_COUNCIL_ADMIN"

const val ROLE_LOCAL_COUNCIL_USER = "ROLE_LOCAL_COUNCIL_USER"

const val ROLE_SYSTEM_OPERATOR = "ROLE_SYSTEM_OPERATOR"

// The set of roles granted access by @AllowIfLandlord, for use where the "any landlord" authorities
// are needed programmatically (e.g. dashboard routing, orphaned-user cleanup).
val LANDLORD_ROLES = setOf(ROLE_INDIVIDUAL_LANDLORD, ROLE_ORG_ADMIN, ROLE_ORG_EDITOR)
