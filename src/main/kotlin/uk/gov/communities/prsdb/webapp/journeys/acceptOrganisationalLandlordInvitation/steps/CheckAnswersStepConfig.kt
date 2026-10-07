package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckAnswersStepConfig")
class CheckAnswersStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractCheckYourAnswersStepConfig<AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "summaryName" to "acceptOrganisationInvitation.checkAnswers.summaryName",
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "summaryListData" to getSummaryList(state),
        )

    override fun afterStepDataIsAdded(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        // TODO PDJB-1774: Save the submitted details as an organisational landlord user with the role from the invitation, then delete the invitation.
        // Also clear its session token and refresh the security context so the invitee receives their new role.
    }

    override fun resolveNextDestination(
        state: AcceptOrganisationalLandlordUserInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getSummaryList(state: AcceptOrganisationalLandlordUserInvitationJourneyState): List<SummaryListRowViewModel> {
        val invitation =
            invitationService.getInvitationForJourneyIdOrNull(state.journeyId)
                ?: throw PrsdbWebException(
                    "Could not find an organisational landlord invitation associated with journey ${state.journeyId}",
                )
        val name = state.fullNameStep.formModel.notNullValue(NameFormModel::name)
        val email = state.emailAddressStep.formModel.notNullValue(AcceptOrganisationInvitationEmailFormModel::emailAddress)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "acceptOrganisationInvitation.checkAnswers.rowHeading.organisation",
                invitation.organisationalLandlord.name,
                Destination.Nowhere(),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "acceptOrganisationInvitation.checkAnswers.rowHeading.name",
                name,
                Destination.VisitableStep(state.fullNameStep, state.getCyaJourneyId(state.fullNameStep)),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "acceptOrganisationInvitation.checkAnswers.rowHeading.emailAddress",
                email,
                Destination.VisitableStep(state.emailAddressStep, state.getCyaJourneyId(state.emailAddressStep)),
            ),
        )
    }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckAnswersStep")
final class CheckAnswersStep(
    stepConfig: CheckAnswersStepConfig,
) : AbstractCheckYourAnswersStep<AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = AbstractCheckYourAnswersStep.ROUTE_SEGMENT
    }
}
