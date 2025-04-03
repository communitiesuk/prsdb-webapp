package uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY

class LandlordDeregistrationJourneyDataExtensions : JourneyDataExtensions() {
    companion object {
        fun JourneyData.getWantsToProceed() =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                DeregisterLandlordStepId.AreYouSure.urlPathSegment,
                LandlordDeregistrationAreYouSureFormModel::wantsToProceed.name,
            )

        fun JourneyData.getLandlordUserHasRegisteredProperties(): Boolean? =
            JourneyDataHelper.getFieldBooleanValue(
                this,
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment,
                USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY,
            )
    }
}
