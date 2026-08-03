package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class MarkLandlordRegistrationCompleteStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        state.userCompletedLandlordRegistrationThisJourney = true

        val landlord = userToLandlordService.getCurrentLandlordForUser()
        state.registeredLandlordRegistrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString()
    }
}

@JourneyFrameworkComponent
final class MarkLandlordRegistrationCompleteStep(
    stepConfig: MarkLandlordRegistrationCompleteStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
