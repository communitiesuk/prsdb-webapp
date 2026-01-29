package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
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
class LocalCouncilUserRegistrationCyaStepConfig(
    private val localCouncilDataService: LocalCouncilDataService,
    private val invitationService: LocalCouncilInvitationService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig<LocalCouncilUserRegistrationJourneyState>() {
    override fun chooseTemplate(state: LocalCouncilUserRegistrationJourneyState) = "forms/checkAnswersForm"

    override fun getStepSpecificContent(state: LocalCouncilUserRegistrationJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "registerLocalCouncilUser.title",
            "summaryName" to "registerLocalCouncilUser.checkAnswers.summaryName",
            "submitButtonText" to "forms.buttons.confirm",
            "summaryListData" to getSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    override fun afterStepDataIsAdded(state: LocalCouncilUserRegistrationJourneyState) {
        val invitation = state.getInvitation()
        val nameFormModel = state.nameStep.formModel
        val emailFormModel = state.emailStep.formModel
        val privacyFormModel = state.privacyNoticeStep.formModel

        val localCouncilUserID =
            localCouncilDataService.registerUserAndReturnID(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                localCouncil = invitation.invitingCouncil,
                name = nameFormModel.name ?: throw IllegalStateException("Name is required"),
                email = emailFormModel.emailAddress ?: throw IllegalStateException("Email is required"),
                invitedAsAdmin = invitation.invitedAsAdmin,
                hasAcceptedPrivacyNotice = privacyFormModel.agreesToPrivacyNotice,
            )

        localCouncilDataService.setLastUserIdRegisteredThisSession(localCouncilUserID)
        invitationService.deleteInvitation(invitation)
        invitationService.clearTokenFromSession()
        securityContextService.refreshContext()
    }

    private fun getSummaryList(state: LocalCouncilUserRegistrationJourneyState): List<SummaryListRowViewModel> {
        val invitation = state.getInvitation()

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.localCouncil",
                invitation.invitingCouncil.name,
                // No change link for council name
                null,
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.name",
                state.nameStep.formModel.name ?: "",
                // CRITICAL: Use Destination.VisitableStep!
                Destination.VisitableStep(state.nameStep, childJourneyId),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.email",
                state.emailStep.formModel.emailAddress ?: "",
                // CRITICAL: Use Destination.VisitableStep!
                Destination.VisitableStep(state.emailStep, childJourneyId),
            ),
        )
    }
}

@JourneyFrameworkComponent
final class LocalCouncilUserRegistrationCyaStep(
    stepConfig: LocalCouncilUserRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LocalCouncilUserRegistrationJourneyState>(stepConfig)
