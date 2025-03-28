package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY

class LandlordDeregistrationJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getWantsToProceed() = this.getWantsToProceed(DeregisterLandlordStepId.AreYouSure.urlPathSegment)

        fun JourneyData.getLandlordUserHasRegisteredProperties(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment,
                USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY,
            )
    }
}
