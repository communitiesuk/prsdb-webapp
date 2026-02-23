package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel

class PropertyDeregistrationStateSessionBuilder : JourneyStateSessionBuilder<PropertyDeregistrationStateSessionBuilder>() {
    fun withAreYouSureCompleted(): PropertyDeregistrationStateSessionBuilder {
        val formModel =
            PropertyDeregistrationAreYouSureFormModel().apply {
                wantsToProceed = true
            }
        withSubmittedValue(AreYouSureStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    companion object {
        fun beforePropertyDeregistrationReason() = PropertyDeregistrationStateSessionBuilder().withAreYouSureCompleted()
    }
}
