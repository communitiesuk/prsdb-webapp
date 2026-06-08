package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class MarkLandlordRegistrationCompleteStepConfig :
    AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        state.userCompletedLandlordRegistrationThisJourney = true
        super.afterStepIsReached(state)
    }
}

@JourneyFrameworkComponent
final class MarkLandlordRegistrationCompleteStep(
    stepConfig: MarkLandlordRegistrationCompleteStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
