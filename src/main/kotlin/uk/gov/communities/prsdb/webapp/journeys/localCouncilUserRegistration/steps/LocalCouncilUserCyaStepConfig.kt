package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.LocalCouncilUserRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
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
    override fun getStepSpecificContent(state: LocalCouncilUserRegistrationJourneyState): Map<String, Any?> =
        mapOf(
            "summaryName" to "registerLocalCouncilUser.checkAnswers.summaryName",
            "submitButtonText" to "forms.buttons.confirm",
            "summaryListData" to getSummaryList(state),
            "submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(state.getSubmittedStepData()),
        )

    override fun afterStepDataIsAdded(state: LocalCouncilUserRegistrationJourneyState) {
        val invitation = state.invitation

        val name = state.nameStep.formModel.notNullValue(NameFormModel::name)
        val email = state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress)
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

    private fun getSummaryList(state: LocalCouncilUserRegistrationJourneyState): List<SummaryListRowViewModel> {
        val invitation = state.invitation
        val localCouncilName = invitation.invitingCouncil.name
        val name = state.nameStep.formModel.notNullValue(NameFormModel::name)
        val email = state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerLocalCouncilUser.checkAnswers.rowHeading.localCouncil",
                localCouncilName,
                Destination.Nowhere(),
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
