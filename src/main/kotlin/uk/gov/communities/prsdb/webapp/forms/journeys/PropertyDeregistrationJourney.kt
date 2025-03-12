package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.http.HttpStatus
import org.springframework.validation.Validator
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.getPropertyDetailsPath
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithSingleLineAddress
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.PropertyDeregistrationJourneyDataHelper.Companion.getWantsToProceed
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyDeregistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
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
                            //   "propertyAddress" to retrieveAddressFromDatabase(),
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
                    formModel = NoInputFormModel::class,
                    templateName = "forms/deregistrationReasonForm",
                    content =
                        mapOf(
                            "title" to "deregisterProperty.title",
                        ),
                ),
        )

    private fun areYouSureContinueToNextActionOrExitJourney(
        journeyData: JourneyData,
        subPageNumber: Int?,
    ): String {
        val areYouSureStep = steps.single { it.id == DeregisterPropertyStepId.AreYouSure }

        if (getWantsToProceed(journeyData)!!) {
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

    companion object {
        val initialStepId = DeregisterPropertyStepId.AreYouSure
    }
}
