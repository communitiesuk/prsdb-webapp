package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.HasPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel

class PropertyDeregistrationStateSessionBuilder : JourneyStateSessionBuilder<PropertyDeregistrationStateSessionBuilder>() {
    fun withAreYouSureCompleted(): PropertyDeregistrationStateSessionBuilder {
        val formModel = PropertyDeregistrationAreYouSureFormModel()
        formModel.wantsToProceed = true
        withSubmittedValue(AreYouSureStep.ROUTE_SEGMENT, formModel)
        return self()
    }

    fun withHasPendingInvitationsCompleted(): PropertyDeregistrationStateSessionBuilder {
        withSubmittedValue(HasPendingInvitationsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withCheckPendingInvitationsCompleted(): PropertyDeregistrationStateSessionBuilder {
        withSubmittedValue(CheckPendingInvitationsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    companion object {
        fun beforePropertyDeregistrationReason() = PropertyDeregistrationStateSessionBuilder().withAreYouSureCompleted()

        fun beforePropertyDeregistrationReasonViaInfo() = PropertyDeregistrationStateSessionBuilder().withHasPendingInvitationsCompleted()

        fun beforePropertyDeregistrationReasonViaCheckPendingInvitations() =
            PropertyDeregistrationStateSessionBuilder()
                .withHasPendingInvitationsCompleted()
                .withCheckPendingInvitationsCompleted()
    }
}
