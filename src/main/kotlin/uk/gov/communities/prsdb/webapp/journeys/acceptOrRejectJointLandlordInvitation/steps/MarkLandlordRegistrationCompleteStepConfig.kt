package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class MarkLandlordRegistrationCompleteStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        state.userCompletedLandlordRegistrationThisJourney = true

        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlord =
            landlordService.retrieveLandlordByBaseUserId(baseUserId)
                ?: throw PrsdbWebException(
                    "Landlord record not found for user with baseUserId $baseUserId after they completed landlord registration",
                )
        state.registeredLandlordRegistrationNumber =
            RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString()
    }
}

@JourneyFrameworkComponent
final class MarkLandlordRegistrationCompleteStep(
    stepConfig: MarkLandlordRegistrationCompleteStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
