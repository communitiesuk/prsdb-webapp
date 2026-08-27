package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentEmailService
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class RemoveDelegationStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val delegateToLettingAgentEmailService: DelegateToLettingAgentEmailService,
) : AbstractInternalStepConfig<Complete, CancelLettingAgentDelegationJourneyState>() {
    override fun mode(state: CancelLettingAgentDelegationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: CancelLettingAgentDelegationJourneyState) {
        val lettingAgentAccess =
            lettingAgentAccessService.getInvitationByPropertyOwnershipId(state.propertyOwnershipId)
                ?: throw PrsdbWebException(
                    "Cannot cancel letting agent delegation for property ownership ${state.propertyOwnershipId}: " +
                        "no letting agent access found",
                )
        val lettingAgentEmail = lettingAgentAccess.invitedEmail
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(state.propertyOwnershipId)
        lettingAgentAccessService.addRemovedLettingAgentToSession(state.propertyOwnershipId, lettingAgentEmail)
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        delegateToLettingAgentEmailService.sendCancellationEmails(propertyOwnership, lettingAgentEmail)
    }

    override fun resolveNextDestination(
        state: CancelLettingAgentDelegationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class RemoveDelegationStep(
    stepConfig: RemoveDelegationStepConfig,
) : JourneyStep.InternalStep<Complete, CancelLettingAgentDelegationJourneyState>(stepConfig)
