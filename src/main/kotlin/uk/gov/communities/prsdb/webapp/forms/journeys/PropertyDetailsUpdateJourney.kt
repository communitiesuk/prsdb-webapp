package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.propertyDetailsUpdateJourneyDataExtensions
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@Component
class PropertyDetailsUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
) : UpdateJourney<UpdatePropertyDetailsStepId>(
        journeyType = JourneyType.PROPERTY_DETAILS_UPDATE,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId = UpdatePropertyDetailsStepId.UpdateOwnershipType
    override val updateStepId = UpdatePropertyDetailsStepId.UpdateDetails

    override val journeyPathSegment: String
        get() = PropertyDetailsController.getUpdatePropertyDetailsPath(getPropertyOwnershipIdFromJourneyKey())

    private val updateDetailsStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateDetails,
            page =
                Page(
                    NoInputFormModel::class,
                    "propertyDetailsView",
                    mapOf(
                        BACK_URL_ATTR_NAME to PropertyDetailsController.PROPERTY_DETAILS_ROUTE,
                    ),
                ),
            handleSubmitAndRedirect = { journeyData, _ -> updatePropertyAndRedirect(journeyData) },
        )

    private val ownershipTypeStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateOwnershipType,
            page =
                Page(
                    formModel = OwnershipTypeFormModel::class,
                    templateName = "forms/ownershipTypeForm.html",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to "forms.update.ownershipType.fieldSetHeading",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = OwnershipType.FREEHOLD,
                                        labelMsgKey = "forms.ownershipType.radios.option.freehold.label",
                                        hintMsgKey = "forms.ownershipType.radios.option.freehold.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = OwnershipType.LEASEHOLD,
                                        labelMsgKey = "forms.ownershipType.radios.option.leasehold.label",
                                        hintMsgKey = "forms.ownershipType.radios.option.leasehold.hint",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateDetails, null) },
            saveAfterSubmit = false,
        )

    private fun updatePropertyAndRedirect(journeyData: JourneyData): String {
        val propertyUpdate =
            PropertyOwnershipUpdateModel(
                ownershipType = journeyData.propertyDetailsUpdateJourneyDataExtensions.getOwnershipTypeUpdateIfPresent(),
            )

        propertyOwnershipService.updatePropertyOwnership(getPropertyOwnershipIdFromJourneyKey(), propertyUpdate)

        journeyDataService.clearJourneyDataFromSession()

        return DETAILS_PATH_SEGMENT
    }

    // The next action flow must have the `updateDetailsStep` after all data changing steps to ensure that validation for all of them is run
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(initialStepId, setOf(ownershipTypeStep, updateDetailsStep))

    override fun createOriginalJourneyData(updateEntityId: String): JourneyData {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(updateEntityId.toLong())

        return mapOf(
            UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment to mapOf("ownershipType" to propertyOwnership.ownershipType),
        )
    }

    fun generateJourneyKey(propertyOwnershipId: Long) = "${journeyType.name}_$propertyOwnershipId"

    private fun getPropertyOwnershipIdFromJourneyKey() =
        journeyDataService
            .getJourneyDataKey()
            .split('_')
            .last()
            .toLong()
}
