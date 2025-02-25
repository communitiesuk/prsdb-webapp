package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL

enum class JourneyType(
    val urlPathSegment: String,
) {
    LANDLORD_REGISTRATION(REGISTER_LANDLORD_JOURNEY_URL),
    LA_USER_REGISTRATION(REGISTER_LA_USER_JOURNEY_URL),
    PROPERTY_REGISTRATION(REGISTER_PROPERTY_JOURNEY_URL),
    UPDATE_LANDLORD_DETAILS(UPDATE_LANDLORD_DETAILS_URL),
}
