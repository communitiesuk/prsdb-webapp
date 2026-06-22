package uk.gov.communities.prsdb.webapp.constants

// These need to be constants as we cannot use the urlPathSegment from the JourneyType directly in the RequestMapping Annotation
const val REGISTER_LANDLORD_JOURNEY_URL: String = "register-as-a-landlord"
const val DEREGISTER_LANDLORD_JOURNEY_URL: String = "deregister-landlord"
const val REGISTER_LOCAL_COUNCIL_USER_JOURNEY_URL: String = "register-local-council-user"
const val REGISTER_PROPERTY_JOURNEY_URL: String = "register-property"
const val DEREGISTER_PROPERTY_JOURNEY_URL: String = "deregister-property"
const val CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL: String = "cancel-joint-landlord-invitation"
const val SWITCH_TO_INDIVIDUAL_JOURNEY_URL: String = "switch-to-individual"
const val NO_LONGER_A_LANDLORD_JOURNEY_URL: String = "no-longer-a-landlord"
