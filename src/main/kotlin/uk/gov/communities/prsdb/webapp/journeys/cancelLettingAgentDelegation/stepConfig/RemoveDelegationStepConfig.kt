package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@JourneyFrameworkComponent
class RemoveDelegationStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : AbstractInternalStepConfig<Complete, CancelLettingAgentDelegationJourneyState>() {
    override fun mode(state: CancelLettingAgentDelegationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: CancelLettingAgentDelegationJourneyState) {
        lettingAgentAccessService.deleteInvitationByPropertyOwnershipId(state.propertyOwnershipId)
        // TODO PDJB-1415: email the landlord to tell them the delegation has been removed
        // TODO PDJB-1415: email the joint landlords (if any) to tell them the delegation has been removed
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
