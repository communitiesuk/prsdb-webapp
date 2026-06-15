package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class HasPendingInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractRequestableStepConfig<HasPendingInvitationsMode, NoInputFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState) =
        mapOf(
            "address" to propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId).address.singleLineAddress,
        )

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "forms/deregisterPropertyInfoForm"

    override fun mode(state: PropertyDeregistrationJourneyState): HasPendingInvitationsMode? =
        getFormModelFromStateOrNull(state)?.let {
            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
            val (pendingInvitations, _) = jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership)
            if (pendingInvitations.isNotEmpty()) {
                HasPendingInvitationsMode.YES
            } else {
                HasPendingInvitationsMode.NO
            }
        }
}

@JourneyFrameworkComponent
final class HasPendingInvitationsStep(
    stepConfig: HasPendingInvitationsStepConfig,
) : RequestableStep<HasPendingInvitationsMode, NoInputFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "info"
    }
}

enum class HasPendingInvitationsMode {
    YES,
    NO,
}
