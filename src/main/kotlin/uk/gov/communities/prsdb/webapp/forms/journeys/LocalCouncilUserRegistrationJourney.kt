package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilPrivacyNoticeController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LocalCouncilUserRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.LocalCouncilUserRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LocalCouncilPrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LocalCouncilUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val invitationService: LocalCouncilInvitationService,
    private val localCouncilDataService: LocalCouncilDataService,
    private val invitation: LocalCouncilInvitation,
    private val securityContextService: SecurityContextService,
) : Journey<RegisterLocalCouncilUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        initialStepId = RegisterLocalCouncilUserStepId.LandingPage,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    init {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (!isJourneyDataInitialized(journeyData)) {
            val emailForm = EmailFormModel.fromLaInvitation(invitation)
            val newJourneyData = journeyData + emailStep().stepDataPair(journeyData, emailForm, subPageNumber = null)
            journeyDataService.setJourneyDataInSession(newJourneyData)
        }
    }

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                landingPageStep(),
                privacyNoticeStep(),
                registerUserStep(),
                emailStep(),
                checkAnswersStep(),
            ),
        )

    private fun isJourneyDataInitialized(journeyData: JourneyData): Boolean =
        journeyData.containsKey(RegisterLocalCouncilUserStepId.Email.urlPathSegment)

    private fun landingPageStep() =
        Step(
            id = RegisterLocalCouncilUserStepId.LandingPage,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "registerLaUser",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                        ),
                ),
            nextAction = { _, _ -> Pair(RegisterLocalCouncilUserStepId.PrivacyNotice, null) },
            saveAfterSubmit = false,
        )

    private fun privacyNoticeStep() =
        Step(
            id = RegisterLocalCouncilUserStepId.PrivacyNotice,
            page =
                Page(
                    formModel = LocalCouncilPrivacyNoticeFormModel::class,
                    templateName = "forms/localAuthorityPrivacyNoticeForm",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                            "submitButtonText" to "forms.buttons.continue",
                            "localAuthorityPrivacyNoticeUrl" to LocalCouncilPrivacyNoticeController.LOCAL_AUTHORITY_PRIVACY_NOTICE_ROUTE,
                            "options" to
                                listOf(
                                    CheckboxViewModel(
                                        value = "true",
                                        labelMsgKey = "registerLAUser.privacyNotice.checkBox.label",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to RegisterLandlordController.LANDLORD_REGISTRATION_START_PAGE_ROUTE,
                        ),
                ),
            nextAction = { _, _ -> Pair(RegisterLocalCouncilUserStepId.Name, null) },
            saveAfterSubmit = false,
        )

    private fun registerUserStep() =
        Step(
            id = RegisterLocalCouncilUserStepId.Name,
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
            nextAction = { _, _ -> Pair(RegisterLocalCouncilUserStepId.Email, null) },
            saveAfterSubmit = false,
        )

    private fun emailStep() =
        Step(
            id = RegisterLocalCouncilUserStepId.Email,
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
            nextAction = { _, _ -> Pair(RegisterLocalCouncilUserStepId.CheckAnswers, null) },
            saveAfterSubmit = false,
        )

    private fun checkAnswersStep() =
        Step(
            id = RegisterLocalCouncilUserStepId.CheckAnswers,
            page = LocalCouncilUserRegistrationCheckAnswersPage(journeyDataService, invitationService, unreachableStepRedirect),
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> checkAnswersHandleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun checkAnswersHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val localAuthorityUserID =
            localCouncilDataService.registerUserAndReturnID(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                localCouncil = invitation.invitingAuthority,
                name = LocalCouncilUserRegistrationJourneyDataHelper.getName(filteredJourneyData)!!,
                email = LocalCouncilUserRegistrationJourneyDataHelper.getEmail(filteredJourneyData)!!,
                invitedAsAdmin = invitation.invitedAsAdmin,
                hasAcceptedPrivacyNotice = LocalCouncilUserRegistrationJourneyDataHelper.getHasAcceptedPrivacyNotice(filteredJourneyData)!!,
            )

        localCouncilDataService.setLastUserIdRegisteredThisSession(localAuthorityUserID)

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
