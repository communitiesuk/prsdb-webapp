package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckInvitationsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.InvitationViewModelBuilder
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class DeregistrationCheckInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractRequestableStepConfig<Complete, CheckInvitationsFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = CheckInvitationsFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState): Map<String, Any?> {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val (pendingInvitations, _) = jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership)
        val invitationViewModels = pendingInvitations.map { InvitationViewModelBuilder.buildPendingViewModel(it) }

        return mapOf(
            "address" to propertyOwnership.address.singleLineAddress,
            "invitations" to invitationViewModels,
            "invitationCount" to invitationViewModels.size,
        )
    }

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "forms/checkInvitationsForm"

    override fun mode(state: PropertyDeregistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class DeregistrationCheckInvitationsStep(
    stepConfig: DeregistrationCheckInvitationsStepConfig,
) : RequestableStep<Complete, CheckInvitationsFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-invitations"
    }
}
