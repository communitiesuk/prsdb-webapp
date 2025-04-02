package uk.gov.communities.prsdb.webapp.forms.journeys
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationCheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LandlordDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val landlordService: LandlordService,
    private val securityContextService: SecurityContextService,
    private val confirmationWithNoPropertiesEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>,
) : Journey<DeregisterLandlordStepId>(
        journeyType = JourneyType.LANDLORD_DEREGISTRATION,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                checkForUserPropertiesStep(),
                areYouSureStep(),
                reasonStep(),
            ),
        )

    private fun checkForUserPropertiesStep() =
        Step(
            id = DeregisterLandlordStepId.CheckForUserProperties,
            page = LandlordDeregistrationCheckUserPropertiesPage(),
            nextAction = { _, _ -> Pair(DeregisterLandlordStepId.AreYouSure, null) },
            saveAfterSubmit = false,
        )

    private fun areYouSureStep() =
        Step(
            id = DeregisterLandlordStepId.AreYouSure,
            page =
                LandlordDeregistrationAreYouSurePage(
                    commonContent =
                        mapOf(
                            "title" to "deregisterLandlord.title",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        valueStr = "yes",
                                        labelMsgKey = "forms.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.radios.option.no.label",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to "/$LANDLORD_DETAILS_PATH_SEGMENT",
                        ),
                    journeyDataService = journeyDataService,
                ),
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { journeyData, subPage -> areYouSureContinueOrExitJourney(journeyData, subPage) },
            // This gets checked when determining whether the next step is reachable
            nextAction = { _, _ -> Pair(DeregisterLandlordStepId.Reason, null) },
            saveAfterSubmit = false,
        )

    private fun reasonStep() =
        Step(
            id = DeregisterLandlordStepId.Reason,
            page =
                Page(
                    formModel = LandlordDeregistrationReasonFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterLandlord.title",
                            "fieldSetHeading" to "forms.reason.landlordDeregistration.fieldSetHeading",
                            "limit" to DEREGISTRATION_REASON_MAX_LENGTH,
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> deregisterLandlordAndProperties(userHadActiveProperties = true) },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueOrExitJourney(
        journeyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        if (journeyData.getWantsToProceed()!!) {
            if (!journeyData.getLandlordUserHasRegisteredProperties()!!) {
                // journeyData.getLandlordUserHasRegisteredProperties() only checked for active, registered properties.
                // To delete the landlord, we must first delete all their properties including inactive ones.
                return deregisterLandlordAndProperties(userHadActiveProperties = false)
            }
            val areYouSureStep = steps.single { it.id == DeregisterLandlordStepId.AreYouSure }
            return getRedirectForNextStep(areYouSureStep, journeyData, subPageNumber)
        }
        return "/$LANDLORD_DETAILS_PATH_SEGMENT"
    }

    private fun deregisterLandlordAndProperties(userHadActiveProperties: Boolean): String {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlordEmailAddress = landlordService.retrieveLandlordByBaseUserId(baseUserId)!!.email
        landlordDeregistrationService.addLandlordHadActivePropertiesToSession(userHadActiveProperties)

        landlordDeregistrationService.deregisterLandlordAndTheirProperties(baseUserId)
        if (!userHadActiveProperties) {
            confirmationWithNoPropertiesEmailSender.sendEmail(landlordEmailAddress, LandlordNoPropertiesDeregistrationConfirmationEmail())
        }

        refreshUserRoles()
        journeyDataService.clearJourneyDataFromSession()

        return CONFIRMATION_PATH_SEGMENT
    }

    private fun refreshUserRoles() {
        securityContextService.refreshContext()
    }

    companion object {
        val initialStepId = DeregisterLandlordStepId.CheckForUserProperties
    }
}
