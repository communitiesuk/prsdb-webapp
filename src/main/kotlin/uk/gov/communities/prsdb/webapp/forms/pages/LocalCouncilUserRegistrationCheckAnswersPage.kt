package uk.gov.communities.prsdb.webapp.forms.pages

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.helpers.LocalCouncilUserRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService

class LocalCouncilUserRegistrationCheckAnswersPage(
    journeyDataService: JourneyDataService,
    private val invitationService: LocalCouncilInvitationService,
    missingAnswersRedirect: String,
) : BasicCheckAnswersPage(
        content =
            mapOf(
                "title" to "registerLAUser.title",
                "summaryName" to "registerLaUser.checkAnswers.summaryName",
                "submitButtonText" to "forms.buttons.confirm",
            ),
        journeyDataService = journeyDataService,
        missingAnswersRedirect = missingAnswersRedirect,
    ) {
    override fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> {
        val sessionToken = invitationService.getTokenFromSession()

        val localAuthority =
            if (sessionToken != null) {
                invitationService.getAuthorityForToken(sessionToken)
            } else {
                throw PrsdbWebException("Local authority not found for this invitation token")
            }

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLaUser.checkAnswers.rowHeading.localAuthority",
                localAuthority.name,
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLaUser.checkAnswers.rowHeading.name",
                LocalCouncilUserRegistrationJourneyDataHelper.getName(filteredJourneyData),
                RegisterLocalCouncilUserStepId.Name.urlPathSegment,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLaUser.checkAnswers.rowHeading.email",
                LocalCouncilUserRegistrationJourneyDataHelper.getEmail(filteredJourneyData),
                RegisterLocalCouncilUserStepId.Email.urlPathSegment,
            ),
        )
    }
}
