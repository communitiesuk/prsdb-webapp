package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class HasPendingInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractInternalStepConfig<HasPendingInvitationsMode, PropertyOwnershipJourneyState>() {
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
) : JourneyStep.InternalStep<HasPendingInvitationsMode, PropertyOwnershipJourneyState>(stepConfig)

enum class HasPendingInvitationsMode {
    YES,
    NO,
}
