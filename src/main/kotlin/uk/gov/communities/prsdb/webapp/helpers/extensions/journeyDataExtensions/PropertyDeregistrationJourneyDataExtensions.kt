package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel

class PropertyDeregistrationJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getWantsToProceed() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                DeregisterPropertyStepId.AreYouSure.urlPathSegment,
                PropertyDeregistrationAreYouSureFormModel::wantsToProceed.name,
            )
    }
}
