package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationAreYouSurePage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordDeregistrationCheckUserPropertiesPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordNoPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordWithPropertiesDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDetailsEmailSectionList
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LandlordDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val landlordService: LandlordService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val securityContextService: SecurityContextService,
    private val confirmationWithNoPropertiesEmailSender: EmailNotificationService<LandlordNoPropertiesDeregistrationConfirmationEmail>,
    private val confirmationWithPropertiesEmailSender: EmailNotificationService<LandlordWithPropertiesDeregistrationConfirmationEmail>,
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
                            BACK_URL_ATTR_NAME to LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE,
                        ),
                    journeyDataService = journeyDataService,
                ),
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { filteredJourneyData, subPage, _ -> areYouSureContinueOrExitJourney(filteredJourneyData, subPage) },
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
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            handleSubmitAndRedirect = { _, _, _ -> deregisterLandlordAndRedirectToConfirmationPage() },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueOrExitJourney(
        filteredJourneyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        if (filteredJourneyData.getWantsToProceed()!!) {
            if (!filteredJourneyData.getLandlordUserHasRegisteredProperties()!!) {
                return deregisterLandlordAndRedirectToConfirmationPage()
            }
            val areYouSureStep = steps.single { it.id == DeregisterLandlordStepId.AreYouSure }
            return getRedirectForNextStep(areYouSureStep, filteredJourneyData, subPageNumber)
        }
        return LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
    }

    private fun deregisterLandlordAndRedirectToConfirmationPage(): String {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val landlordEmailAddress = landlordService.retrieveLandlordByBaseUserId(baseUserId)!!.email

        val landlordProperties = propertyOwnershipService.retrieveAllActivePropertiesForLandlord(baseUserId)
        landlordDeregistrationService.addLandlordHadActivePropertiesToSession(landlordProperties.isNotEmpty())

        landlordDeregistrationService.deregisterLandlord(baseUserId)

        if (landlordProperties.isEmpty()) {
            confirmationWithNoPropertiesEmailSender.sendEmail(landlordEmailAddress, LandlordNoPropertiesDeregistrationConfirmationEmail())
        } else {
            val propertySectionList = PropertyDetailsEmailSectionList.fromPropertyOwnerships(landlordProperties)

            confirmationWithPropertiesEmailSender.sendEmail(
                landlordEmailAddress,
                LandlordWithPropertiesDeregistrationConfirmationEmail(propertySectionList),
            )
        }

        refreshUserRoles()
        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return CONFIRMATION_PATH_SEGMENT
    }

    private fun refreshUserRoles() {
        securityContextService.refreshContext()
    }

    companion object {
        val initialStepId = DeregisterLandlordStepId.CheckForUserProperties
    }
}
