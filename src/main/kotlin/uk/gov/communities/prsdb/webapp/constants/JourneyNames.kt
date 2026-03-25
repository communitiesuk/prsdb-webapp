package uk.gov.communities.prsdb.webapp.constants

// These need to be constants as we cannot use the urlPathSegment from the JourneyType directly in the RequestMapping Annotation
const val REGISTER_LANDLORD_JOURNEY_URL: String = "register-as-a-landlord"
const val DEREGISTER_LANDLORD_JOURNEY_URL: String = "deregister-landlord"
const val REGISTER_LOCAL_COUNCIL_USER_JOURNEY_URL: String = "register-local-council-user"
const val REGISTER_PROPERTY_JOURNEY_URL: String = "register-property"
const val DEREGISTER_PROPERTY_JOURNEY_URL: String = "deregister-property"
