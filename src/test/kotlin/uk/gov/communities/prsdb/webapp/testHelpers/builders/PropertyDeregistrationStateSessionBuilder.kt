package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.DeregistrationCheckInvitationsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel

class PropertyDeregistrationStateSessionBuilder : JourneyStateSessionBuilder<PropertyDeregistrationStateSessionBuilder>() {
    fun withAreYouSureCompleted(): PropertyDeregistrationStateSessionBuilder {
        val formModel = PropertyDeregistrationAreYouSureFormModel()
        formModel.wantsToProceed = true
        withSubmittedValue(AreYouSureStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withCheckInvitationsCompleted(): PropertyDeregistrationStateSessionBuilder {
        withSubmittedValue(DeregistrationCheckInvitationsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    companion object {
        fun beforePropertyDeregistrationReason() = PropertyDeregistrationStateSessionBuilder().withAreYouSureCompleted()

        fun beforePropertyDeregistrationReasonViaCheckInvitations() =
            PropertyDeregistrationStateSessionBuilder()
                .withAreYouSureCompleted()
                .withCheckInvitationsCompleted()
    }
}
