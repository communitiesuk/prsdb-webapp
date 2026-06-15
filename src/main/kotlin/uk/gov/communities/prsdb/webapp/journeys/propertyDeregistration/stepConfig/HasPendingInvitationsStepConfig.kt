package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class HasPendingInvitationsStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractInternalStepConfig<HasPendingInvitationsMode, PropertyDeregistrationJourneyState>() {
    override fun mode(state: PropertyDeregistrationJourneyState): HasPendingInvitationsMode {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        val (pendingInvitations, _) = jointLandlordInvitationService.getPendingAndExpiredInvitations(propertyOwnership)
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
) : JourneyStep.InternalStep<HasPendingInvitationsMode, PropertyDeregistrationJourneyState>(stepConfig)

enum class HasPendingInvitationsMode {
    YES,
    NO,
}
