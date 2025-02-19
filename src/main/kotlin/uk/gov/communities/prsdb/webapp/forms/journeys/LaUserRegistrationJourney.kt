package uk.gov.communities.prsdb.webapp.forms.journeys

import jakarta.servlet.http.HttpSession
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.LA_USER_ID
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.pages.LaUserRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.LaUserRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@Component
class LaUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val invitationService: LocalAuthorityInvitationService,
    localAuthorityDataService: LocalAuthorityDataService,
    session: HttpSession,
) : Journey<RegisterLaUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId: RegisterLaUserStepId = RegisterLaUserStepId.LandingPage

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                landingPageStep(),
                registerUserStep(),
                emailStep(),
                checkAnswersStep(journeyDataService, invitationService, localAuthorityDataService, session),
            ),
        )

    fun initialiseJourneyData(token: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val formData: PageData = mutableMapOf("emailAddress" to invitationService.getEmailAddressForToken(token))
        val emailStep = steps.single { step -> step.id == RegisterLaUserStepId.Email }

        emailStep.updateJourneyData(journeyData, formData, null)
        journeyDataService.setJourneyData(journeyData)
    }

    private fun landingPageStep() =
        Step(
            id = RegisterLaUserStepId.LandingPage,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "registerLaUser",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                        ),
                ),
            nextAction = { _, _ -> Pair(RegisterLaUserStepId.Name, null) },
            saveAfterSubmit = false,
        )

    private fun registerUserStep() =
        Step(
            id = RegisterLaUserStepId.Name,
            page =
                Page(
                    formModel = NameFormModel::class,
                    templateName = "forms/nameForm",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                            "fieldSetHeading" to "forms.name.fieldSetHeading",
                            "fieldSetHint" to "forms.name.fieldSetHint",
                            "label" to "forms.name.label",
                            "submitButtonText" to "forms.buttons.continue",
                            "backUrl" to "/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/",
                        ),
                ),
            nextAction = { _, _ -> Pair(RegisterLaUserStepId.Email, null) },
            saveAfterSubmit = false,
        )

    private fun emailStep() =
        Step(
            id = RegisterLaUserStepId.Email,
            page =
                Page(
                    formModel = EmailFormModel::class,
                    templateName = "forms/emailForm",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                            "fieldSetHeading" to "registerLAUser.email.fieldSetHeading",
                            "fieldSetHint" to "registerLAUser.email.fieldSetHint",
                            "label" to "registerLAUser.email.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            nextAction = { _, _ -> Pair(RegisterLaUserStepId.CheckAnswers, null) },
            saveAfterSubmit = false,
        )

    private fun checkAnswersStep(
        journeyDataService: JourneyDataService,
        invitationService: LocalAuthorityInvitationService,
        localAuthorityDataService: LocalAuthorityDataService,
        session: HttpSession,
    ) = Step(
        id = RegisterLaUserStepId.CheckAnswers,
        page =
            LaUserRegistrationCheckAnswersPage(
                formModel = CheckAnswersFormModel::class,
                templateName = "forms/checkAnswersForm",
                content =
                    mapOf(
                        "title" to "registerLAUser.title",
                        "summaryName" to "registerLaUser.checkAnswers.summaryName",
                        "submitButtonText" to "forms.buttons.confirm",
                    ),
                invitationService,
            ),
        handleSubmitAndRedirect = { journeyData, _ ->
            checkAnswersHandleSubmitAndRedirect(
                journeyData,
                journeyDataService,
                invitationService,
                localAuthorityDataService,
                session,
            )
        },
        saveAfterSubmit = false,
    )

    private fun checkAnswersHandleSubmitAndRedirect(
        journeyData: JourneyData,
        journeyDataService: JourneyDataService,
        invitationService: LocalAuthorityInvitationService,
        localAuthorityDataService: LocalAuthorityDataService,
        session: HttpSession,
    ): String {
        val token = invitationService.getTokenFromSession()!!

        val localAuthorityUserID =
            localAuthorityDataService.registerUserAndReturnID(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                localAuthority = invitationService.getAuthorityForToken(token),
                name = LaUserRegistrationJourneyDataHelper.getName(journeyData)!!,
                email = LaUserRegistrationJourneyDataHelper.getEmail(journeyData)!!,
            )

        val invitation = invitationService.getInvitationFromToken(token)
        invitationService.deleteInvitation(invitation)
        invitationService.clearTokenFromSession()

        journeyDataService.clearJourneyDataFromSession()

        session.setAttribute(LA_USER_ID, localAuthorityUserID)

        return CONFIRMATION_PAGE_PATH_SEGMENT
    }
}
