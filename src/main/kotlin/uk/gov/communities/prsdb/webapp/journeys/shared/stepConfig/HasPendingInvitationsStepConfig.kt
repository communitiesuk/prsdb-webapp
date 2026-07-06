package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class HasPendingInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractRequestableStepConfig<HasPendingInvitationsMode, NoInputFormModel, PropertyOwnershipJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: PropertyOwnershipJourneyState): Map<String, Any?> = mapOf<String, String>()

    override fun chooseTemplate(state: PropertyOwnershipJourneyState): String = ""

    override fun mode(state: PropertyOwnershipJourneyState): HasPendingInvitationsMode {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val pendingInvitations = jointLandlordInvitationService.getPendingInvitations(propertyOwnership)
        return if (pendingInvitations.isNotEmpty()) {
            HasPendingInvitationsMode.YES
        } else {
            HasPendingInvitationsMode.NO
        }
    }
}

@JourneyFrameworkComponent
final class HasPendingInvitationsStep(
    stepConfig: HasPendingInvitationsStepConfig,
) : JourneyStep.RequestableStep<HasPendingInvitationsMode, NoInputFormModel, PropertyOwnershipJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "start"
    }
}

enum class HasPendingInvitationsMode {
    YES,
    NO,
}
