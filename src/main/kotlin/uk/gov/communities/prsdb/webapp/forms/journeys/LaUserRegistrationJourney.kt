package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LaUserRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.LaUserRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LaUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val invitationService: LocalAuthorityInvitationService,
    private val localAuthorityDataService: LocalAuthorityDataService,
    private val invitation: LocalAuthorityInvitation,
    private val securityContextService: SecurityContextService,
) : Journey<RegisterLaUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        initialStepId = RegisterLaUserStepId.LandingPage,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    init {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (!isJourneyDataInitialized(journeyData)) {
            val emailForm = EmailFormModel.fromLaInvitation(invitation)
            val newJourneyData = emailStep().updatedJourneyData(journeyData, emailForm, subPageNumber = null)
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                landingPageStep(),
                registerUserStep(),
                emailStep(),
                checkAnswersStep(),
            ),
        )

    private fun isJourneyDataInitialized(journeyData: JourneyData): Boolean =
        journeyData.containsKey(RegisterLaUserStepId.Email.urlPathSegment)

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

    private fun checkAnswersStep() =
        Step(
            id = RegisterLaUserStepId.CheckAnswers,
            page = LaUserRegistrationCheckAnswersPage(journeyDataService, invitationService),
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> checkAnswersHandleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun checkAnswersHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val localAuthorityUserID =
            localAuthorityDataService.registerUserAndReturnID(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                localAuthority = invitation.invitingAuthority,
                name = LaUserRegistrationJourneyDataHelper.getName(filteredJourneyData)!!,
                email = LaUserRegistrationJourneyDataHelper.getEmail(filteredJourneyData)!!,
                invitedAsAdmin = invitation.invitedAsAdmin,
            )

        localAuthorityDataService.setLastUserIdRegisteredThisSession(localAuthorityUserID)

        invitationService.deleteInvitation(invitation)
        invitationService.clearTokenFromSession()

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        refreshUserRoles()

        return CONFIRMATION_PATH_SEGMENT
    }

    private fun refreshUserRoles() {
        securityContextService.refreshContext()
    }
}
