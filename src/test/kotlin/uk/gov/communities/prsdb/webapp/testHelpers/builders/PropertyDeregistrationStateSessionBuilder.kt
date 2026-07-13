package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.DeregisterInfoStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

class PropertyDeregistrationStateSessionBuilder : JourneyStateSessionBuilder<PropertyDeregistrationStateSessionBuilder>() {
    fun withDeregisterInfoCompleted(): PropertyDeregistrationStateSessionBuilder {
        withSubmittedValue(DeregisterInfoStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withCheckPendingInvitationsCompleted(): PropertyDeregistrationStateSessionBuilder {
        withSubmittedValue(CheckPendingInvitationsStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    companion object {
        fun beforePropertyDeregistrationReasonViaInfo() = PropertyDeregistrationStateSessionBuilder().withDeregisterInfoCompleted()

        fun beforePropertyDeregistrationReasonViaCheckPendingInvitations() =
            PropertyDeregistrationStateSessionBuilder()
                .withDeregisterInfoCompleted()
                .withCheckPendingInvitationsCompleted()
    }
}
