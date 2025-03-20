package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

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

        // fun JourneyData.get
    }
}
