package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class DeregistrationJourneyDataExtensions {
    companion object {
        fun JourneyData.getWantsToProceedPropertyDeregistration() =
            this.getWantsToProceed(DeregisterPropertyStepId.AreYouSure.urlPathSegment)

        fun JourneyData.getWantsToProceedLandlordDeregistration() =
            this.getWantsToProceed(DeregisterLandlordStepId.AreYouSure.urlPathSegment)

        private fun JourneyData.getWantsToProceed(urlPathSegment: String): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                urlPathSegment,
                "wantsToProceed",
            )

        fun JourneyData.getLandlordUserHasRegisteredProperties(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT,
                USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY,
            )
    }
}
