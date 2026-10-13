package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationJoinOrganisationStepConfig")
class JoinOrganisationStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun beforeAttemptingToReachStep(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Boolean =
        invitationService.getPendingInvitationForJourneyIdOrNull(state.journeyId) != null

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to
                (
                    invitationService
                        .getPendingInvitationForJourneyIdOrNull(state.journeyId)
                        ?.organisationalLandlord
                        ?.name
                        // beforeAttemptingToReachStep has already checked for a pending invitation, so this is only reachable if the
                        // invitation stopped being pending between that check and this one.
                        ?: throw PrsdbWebException(
                            "Could not find a pending invitation for invitation journey ${state.journeyId}",
                        )
                ),
        )

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/joinOrganisationStart"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            Complete.COMPLETE
        }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationJoinOrganisationStep")
final class JoinOrganisationStep(
    stepConfig: JoinOrganisationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "join-your-organisation"
    }
}
