package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.InvitationViewModelBuilder
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CheckPendingInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyOwnershipJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun chooseTemplate(state: PropertyOwnershipJourneyState) = "forms/checkInvitationsForm"

    override fun getStepSpecificContent(state: PropertyOwnershipJourneyState): Map<String, Any?> {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val pendingInvitations = jointLandlordInvitationService.getPendingInvitations(propertyOwnership)
        val invitationViewModels = pendingInvitations.map { InvitationViewModelBuilder.buildPendingViewModel(it) }

        return mapOf(
            "address" to propertyOwnership.address.singleLineAddress,
            "invitations" to invitationViewModels,
            "invitationCount" to invitationViewModels.size,
            "cancelUrl" to PropertyDetailsController.getPropertyDetailsPath(state.propertyOwnershipId),
        )
    }

    override fun mode(state: PropertyOwnershipJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
class CheckPendingInvitationsStep(
    stepConfig: CheckPendingInvitationsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyOwnershipJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-invitations"
    }
}
