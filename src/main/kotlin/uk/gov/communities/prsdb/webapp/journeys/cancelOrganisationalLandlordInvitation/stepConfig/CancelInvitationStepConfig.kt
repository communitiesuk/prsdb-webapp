package uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("cancelOrganisationalLandlordInvitationCancelInvitationStepConfig")
class CancelInvitationStepConfig(
    private val organisationalLandlordInvitationService: OrganisationalLandlordInvitationService,
) : AbstractInternalStepConfig<Complete, CancelOrganisationalLandlordInvitationJourneyState>() {
    override fun mode(state: CancelOrganisationalLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: CancelOrganisationalLandlordInvitationJourneyState) {
        organisationalLandlordInvitationService.cancelInvitation(state.invitationId)
    }

    override fun resolveNextDestination(
        state: CancelOrganisationalLandlordInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent("cancelOrganisationalLandlordInvitationCancelInvitationStep")
class CancelInvitationStep(
    stepConfig: CancelInvitationStepConfig,
) : JourneyStep.InternalStep<Complete, CancelOrganisationalLandlordInvitationJourneyState>(stepConfig)
