package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.getPropertyDetailsPath
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.PropertyDeregistrationJourneyDataHelper.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val addressDataService: AddressDataService,
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
                Page(
                    formModel = PropertyDeregistrationAreYouSureFormModel::class,
                    templateName = "forms/areYouSureForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                            "fieldSetHeading" to "deregisterProperty.areYouSure.fieldSetHeading",
                            "propertyAddress" to retrieveAddressFromCacheOrDatabase(),
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        labelMsgKey = "forms.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        labelMsgKey = "forms.radios.option.no.label",
                                    ),
                                ),
                            "backUrl" to getPropertyDetailsPath(propertyOwnershipId),
                        ),
                ),
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
                    formModel = NoInputFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                        ),
                ),
        )

    private fun areYouSureContinueToNextActionOrExitJourney(
        newJourneyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val currentStep = areYouSureStep()

        if (getWantsToProceed(newJourneyData)!!) {
            return getRedirectForNextStep(currentStep, newJourneyData, subPageNumber)
        }

        addressDataService.clearCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId)
        return getPropertyDetailsPath(propertyOwnershipId)
    }

    private fun retrieveAddressFromCacheOrDatabase(): String {
        val cachedAddress = addressDataService.getCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId)
        if (cachedAddress != null) {
            return cachedAddress
        }

        val addressFromDatabase = retrieveAddressFromDatabase()
        addressDataService.cacheSingleLineAddressForPropertyOwnershipId(propertyOwnershipId, addressFromDatabase)
        return addressFromDatabase
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

    companion object {
        val initialStepId = DeregisterPropertyStepId.AreYouSure
    }
}
