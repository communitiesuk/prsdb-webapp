package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithContentProvider
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeregistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyDeregistrationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyDeregistrationService: PropertyDeregistrationService,
    private val confirmationEmailSender: EmailNotificationService<PropertyDeregistrationConfirmationEmail>,
    private val propertyOwnershipId: Long,
) : Journey<DeregisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_DEREGISTRATION,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                areYouSureStep(),
                reasonStep(),
            ),
        )

    private fun areYouSureStep() =
        Step(
            id = DeregisterPropertyStepId.AreYouSure,
            page =
                PageWithContentProvider(
                    formModel = PropertyDeregistrationAreYouSureFormModel::class,
                    templateName = "forms/areYouSureForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "forms.areYouSure.propertyDeregistration.fieldSetHeading",
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
                            BACK_URL_ATTR_NAME to PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId),
                        ),
                ) { mapOf("optionalFieldSetHeadingParam" to getPropertySingleLineAddress()) },
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { filteredJourneyData, subPage, _ ->
                areYouSureContinueToNextActionOrExitJourney(filteredJourneyData, subPage)
            },
            // This gets checked when determining whether the next step is reachable
            nextAction = { _, _ -> Pair(DeregisterPropertyStepId.Reason, null) },
            saveAfterSubmit = false,
        )

    private fun reasonStep() =
        Step(
            id = DeregisterPropertyStepId.Reason,
            page =
                Page(
                    formModel = PropertyDeregistrationReasonFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "forms.reason.propertyDeregistration.fieldSetHeading",
                            "fieldSetHint" to "forms.reason.propertyDeregistration.fieldSetHint",
                            "limit" to DEREGISTRATION_REASON_MAX_LENGTH,
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            handleSubmitAndRedirect = { _, _, _ -> deregisterPropertyAndRedirectToConfirmation() },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueToNextActionOrExitJourney(
        filteredJourneyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val areYouSureStep = steps.single { it.id == DeregisterPropertyStepId.AreYouSure }

        if (filteredJourneyData.getWantsToProceed()!!) {
            return getRedirectForNextStep(areYouSureStep, filteredJourneyData, subPageNumber)
        }

        return PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)
    }

    private fun getPropertySingleLineAddress() =
        propertyOwnershipService
            .retrievePropertyOwnershipById(propertyOwnershipId)
            ?.property
            ?.address
            ?.singleLineAddress
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Address for property ownership id $propertyOwnershipId not found",
            )

    private fun deregisterPropertyAndRedirectToConfirmation(): String {
        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Property ownership $propertyOwnershipId not found")
        val primaryLandlordEmailAddress = propertyOwnership.primaryLandlord.email
        val propertyRegistrationNumber = propertyOwnership.registrationNumber
        val propertyAddress = propertyOwnership.property.address.singleLineAddress

        propertyDeregistrationService.deregisterProperty(propertyOwnershipId)

        propertyDeregistrationService.addDeregisteredPropertyAndOwnershipIdsToSession(propertyOwnershipId, propertyOwnership.property.id)

        confirmationEmailSender.sendEmail(
            primaryLandlordEmailAddress,
            PropertyDeregistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(propertyRegistrationNumber).toString(),
                propertyAddress,
            ),
        )

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return CONFIRMATION_PATH_SEGMENT
    }

    companion object {
        val initialStepId = DeregisterPropertyStepId.AreYouSure
    }
}
