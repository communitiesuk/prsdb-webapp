package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class CheckInvitationsStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, InviteJointLandlordJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun chooseTemplate(state: InviteJointLandlordJourneyState): String = "forms/checkAnswersForm"

    override fun mode(state: InviteJointLandlordJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun getStepSpecificContent(state: InviteJointLandlordJourneyState) =
        mapOf(
            "summaryName" to "inviteJointLandlord.checkInvitations.summaryName",
            "summaryListData" to getInvitationsSummaryRow(state),
            "submitButtonText" to "inviteJointLandlord.checkInvitations.submitButtonText",
        )

    private fun getInvitationsSummaryRow(state: InviteJointLandlordJourneyState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "forms.checkPropertyAnswers.jointLandlordsDetails.invitations",
                state.invitedJointLandlords,
                destination = Destination(state.checkJointLandlordsStep),
            ),
        )
}

@JourneyFrameworkComponent
class CheckInvitationsStep(
    stepConfig: CheckInvitationsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, InviteJointLandlordJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-invitations"
    }
}
