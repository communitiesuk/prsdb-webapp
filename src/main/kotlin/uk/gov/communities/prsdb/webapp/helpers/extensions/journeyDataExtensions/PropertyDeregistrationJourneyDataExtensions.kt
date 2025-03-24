package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId

class PropertyDeregistrationJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getWantsToProceed() = this.getWantsToProceed(DeregisterPropertyStepId.AreYouSure.urlPathSegment)
    }
}
