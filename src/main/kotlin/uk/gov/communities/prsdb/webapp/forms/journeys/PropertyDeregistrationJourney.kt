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
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService

class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyRegistrationService: PropertyRegistrationService,
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
                ) { mapOf("singleLineAddress" to getPropertySingleLineAddress()) },
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { newJourneyData, subPage -> areYouSureContinueToNextActionOrExitJourney(newJourneyData, subPage) },
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
            handleSubmitAndRedirect = { _, _ -> deregisterPropertyAndRedirectToConfirmation() },
            saveAfterSubmit = false,
        )

    private fun areYouSureContinueToNextActionOrExitJourney(
        journeyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val areYouSureStep = steps.single { it.id == DeregisterPropertyStepId.AreYouSure }

        if (journeyData.getWantsToProceed()!!) {
            return getRedirectForNextStep(areYouSureStep, journeyData, subPageNumber)
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
        propertyRegistrationService.deregisterProperty(propertyOwnershipId)
        propertyRegistrationService.setDeregisteredPropertyOwnershipIdInSession(propertyOwnershipId)

        return CONFIRMATION_PATH_SEGMENT
    }

    companion object {
        val initialStepId = DeregisterPropertyStepId.AreYouSure
    }
}
