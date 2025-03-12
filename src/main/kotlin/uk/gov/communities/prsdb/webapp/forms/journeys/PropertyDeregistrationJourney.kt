package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.DEREGISTRATION_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.getPropertyDetailsPath
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithSingleLineAddress
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDeregistrationJourneyDataExtensions.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeregistrationReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyService

class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyService: PropertyService,
    private val propertyOwnershipId: Long,
) : Journey<DeregisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_DEREGISTRATION,
        journeyDataKey = "${DEREGISTER_PROPERTY_JOURNEY_URL}_$propertyOwnershipId",
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
                PageWithSingleLineAddress(
                    formModel = PropertyDeregistrationAreYouSureFormModel::class,
                    templateName = "forms/areYouSureForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "deregisterProperty.areYouSure.fieldSetHeading",
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
                            BACK_URL_ATTR_NAME to getPropertyDetailsPath(propertyOwnershipId),
                        ),
                ) { retrieveAddressFromDatabase() },
            // handleSubmitAndRedirect will execute. It does not have to redirect to the step specified in nextAction.
            handleSubmitAndRedirect = { newJourneyData, subPage -> areYouSureContinueToNextActionOrExitJourney(newJourneyData, subPage) },
            // This gets checked when determining whether the next step is reachable
            nextAction = { _, _ -> Pair(DeregisterPropertyStepId.Reason, null) },
        )

    private fun reasonStep() =
        Step(
            id = DeregisterPropertyStepId.Reason,
            page =
                Page(
                    formModel = DeregistrationReasonFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "deregisterProperty.reason.fieldSetHeading",
                            "limit" to DEREGISTRATION_REASON_MAX_LENGTH,
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> deregisterPropertyAndRedirectToConfirmation() },
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

    private fun retrieveAddressFromDatabase(): String =
        propertyOwnershipService
            .retrievePropertyOwnershipById(propertyOwnershipId)
            ?.property
            ?.address
            ?.singleLineAddress ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Address for property ownership id $propertyOwnershipId not found",
        )

    private fun deregisterPropertyAndRedirectToConfirmation(): String {
        checkIfLoggedInUserIsAuthorisedToDeleteRecord(propertyOwnershipId, propertyOwnershipService)

        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)?.let {
            propertyOwnershipService.deletePropertyOwnership(it)
            propertyService.deleteProperty(it.property)
        }

        // TODO: PRSD-698 - redirect to confirmation page
        return LANDLORD_DASHBOARD_URL
    }

    companion object {
        val initialStepId = DeregisterPropertyStepId.AreYouSure

        fun checkIfLoggedInUserIsAuthorisedToDeleteRecord(
            propertyOwnershipId: Long,
            propertyOwnershipService: PropertyOwnershipService,
        ) {
            val baseUserId = SecurityContextHolder.getContext().authentication.name
            if (!propertyOwnershipService.getIsAuthorizedToDeleteRecord(propertyOwnershipId, baseUserId)) {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "The current user is not authorised to delete property ownership $propertyOwnershipId",
                )
            }
        }
    }
}
