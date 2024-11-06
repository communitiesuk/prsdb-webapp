package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL

enum class JourneyType(
    val urlPathSegment: String,
) {
    LANDLORD_REGISTRATION(REGISTER_LANDLORD_JOURNEY_URL),
}
