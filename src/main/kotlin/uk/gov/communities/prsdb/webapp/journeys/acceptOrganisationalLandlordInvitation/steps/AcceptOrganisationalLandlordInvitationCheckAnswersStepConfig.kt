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
class AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractCheckYourAnswersStepConfig<AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override fun beforeAttemptingToReachStep(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Boolean =
        invitationService.getPendingInvitationForJourneyIdOrNull(state.journeyId) != null

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "pageTitleOverride" to "acceptOrganisationInvitation.checkAnswers.pageTitle",
            "summaryName" to "acceptOrganisationInvitation.checkAnswers.summaryName",
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "summaryListData" to getSummaryList(state),
        )

    override fun afterStepDataIsAdded(state: AcceptOrganisationalLandlordUserInvitationJourneyState) {
        // TODO PDJB-1774: Add a separate internal step to save the submitted details as an organisational landlord user with the role from the invitation,
        // delete the invitation, clear its session token, and refresh the security context so the invitee receives their new role.
        // That step should also re-check the invitation is still pending before saving, as it may have been cancelled or accepted elsewhere since
        // beforeAttemptingToReachStep ran. If it is no longer valid, the invitee should end up at invalidLinkStep rather than a generic error page.
        // It should re-check the invitee is still not a landlord for the same reason, as the check user is landlord step only runs at the start of
        // the journey and the invitee could have registered as a landlord in another tab since then.
    }

    // override this as the next step will handle deleting the journey (TODO PDJB-1774)
    override fun resolveNextDestination(
        state: AcceptOrganisationalLandlordUserInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getSummaryList(state: AcceptOrganisationalLandlordUserInvitationJourneyState): List<SummaryListRowViewModel> {
        val organisationName =
            invitationService
                .getPendingInvitationForJourneyIdOrNull(state.journeyId)
                ?.organisationalLandlord
                ?.name
                // beforeAttemptingToReachStep has already checked for a pending invitation, so this is only reachable if the
                // invitation stopped being pending between that check and this one.
                ?: throw PrsdbWebException(
                    "Could not find a pending invitation for invitation journey ${state.journeyId}",
                )
        val name = state.fullNameStep.formModel.notNullValue(NameFormModel::name)
        val email = state.emailAddressStep.formModel.notNullValue(AcceptOrganisationInvitationEmailFormModel::emailAddress)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "acceptOrganisationInvitation.checkAnswers.rowHeading.organisation",
                organisationName,
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
final class AcceptOrganisationalLandlordInvitationCheckAnswersStep(
    stepConfig: AcceptOrganisationalLandlordInvitationCheckAnswersStepConfig,
) : AbstractCheckYourAnswersStep<AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = AbstractCheckYourAnswersStep.ROUTE_SEGMENT
    }
}
