package uk.gov.communities.prsdb.webapp.constants

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController

// These need to be constants as we cannot use the urlPathSegment from the JourneyType directly in the RequestMapping Annotation
const val REGISTER_LANDLORD_JOURNEY_URL: String = "register-as-a-landlord"
const val REGISTER_LA_USER_JOURNEY_URL: String = "register-local-authority-user"
const val REGISTER_PROPERTY_JOURNEY_URL: String = "register-property"
const val DEREGISTER_PROPERTY_JOURNEY_URL: String = "de-register-property"
const val UPDATE_LANDLORD_DETAILS_URL: String = LandlordDetailsController.UPDATE_ROUTE
