package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId

class PropertyDeregistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getWantsToProceed(journeyData: JourneyData) =
            getFieldBooleanValue(
                journeyData,
                DeregisterPropertyStepId.AreYouSure.urlPathSegment,
                "wantsToProceed",
            )
    }
}
