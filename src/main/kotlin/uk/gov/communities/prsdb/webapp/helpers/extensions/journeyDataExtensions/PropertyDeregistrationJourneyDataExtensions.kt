package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper

class PropertyDeregistrationJourneyDataExtensions {
    companion object {
        fun JourneyData.getWantsToProceed() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                DeregisterPropertyStepId.AreYouSure.urlPathSegment,
                "wantsToProceed",
            )
    }
}
