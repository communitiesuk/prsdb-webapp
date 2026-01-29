package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.LocalCouncilUserRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class LocalCouncilUserCyaStepConfig(
    private val localCouncilDataService: LocalCouncilDataService,
    private val invitationService: LocalCouncilInvitationService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig<LocalCouncilUserRegistrationJourneyState>() {
    override fun chooseTemplate(state: LocalCouncilUserRegistrationJourneyState) = "forms/checkAnswersForm"

    override fun getStepSpecificContent(state: LocalCouncilUserRegistrationJourneyState): Map<String, Any?> {
        val localCouncilName = getLocalCouncilName(state)

        return mapOf(
            "summaryName" to "registerLocalCouncilUser.checkAnswers.summaryName",
            "submitButtonText" to "forms.buttons.confirm",
            "summaryListData" to getSummaryList(state, localCouncilName),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )
    }

    override fun afterStepDataIsAdded(state: LocalCouncilUserRegistrationJourneyState) {
        val token =
            invitationService.getTokenFromSession()
                ?: throw PrsdbWebException("Invitation token not found in session")

        val invitation = invitationService.getInvitationFromToken(token)

        val name =
            state.nameStep.formModel.name
                ?: throw PrsdbWebException("Name not found in journey state")
        val email =
            state.emailStep.formModel.emailAddress
                ?: throw PrsdbWebException("Email not found in journey state")
        val hasAcceptedPrivacyNotice = state.privacyNoticeStep.formModel.agreesToPrivacyNotice

        val localCouncilUserId =
            localCouncilDataService.registerUserAndReturnID(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                localCouncil = invitation.invitingCouncil,
                name = name,
                email = email,
                invitedAsAdmin = invitation.invitedAsAdmin,
                hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice,
            )

        localCouncilDataService.setLastUserIdRegisteredThisSession(localCouncilUserId)

        invitationService.deleteInvitation(invitation)
        invitationService.clearTokenFromSession()

        securityContextService.refreshContext()
    }

    private fun getLocalCouncilName(state: LocalCouncilUserRegistrationJourneyState): String {
        val token =
            invitationService.getTokenFromSession()
                ?: throw PrsdbWebException("Invitation token not found in session")

        return invitationService.getAuthorityForToken(token).name
    }

    private fun getSummaryList(
        state: LocalCouncilUserRegistrationJourneyState,
        localCouncilName: String,
    ): List<SummaryListRowViewModel> {
        val name =
            state.nameStep.formModel.name
                ?: throw PrsdbWebException("Name not found in journey state")
        val email =
            state.emailStep.formModel.emailAddress
                ?: throw PrsdbWebException("Email not found in journey state")

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.localCouncil",
                localCouncilName,
                actionUrl = null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.name",
                name,
                Destination.VisitableStep(state.nameStep, childJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.email",
                email,
                Destination.VisitableStep(state.emailStep, childJourneyId),
            ),
        )
    }
}

@JourneyFrameworkComponent
final class LocalCouncilUserCyaStep(
    stepConfig: LocalCouncilUserCyaStepConfig,
) : AbstractCheckYourAnswersStep<LocalCouncilUserRegistrationJourneyState>(stepConfig)
