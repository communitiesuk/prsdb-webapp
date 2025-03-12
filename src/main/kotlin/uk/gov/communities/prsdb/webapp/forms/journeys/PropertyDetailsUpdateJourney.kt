package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyDetailsUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipId: Long,
) : UpdateJourney<UpdatePropertyDetailsStepId>(
        journeyType = JourneyType.PROPERTY_DETAILS_UPDATE,
        initialStepId = UpdatePropertyDetailsStepId.UpdateOwnershipType,
        validator = validator,
        journeyDataService = journeyDataService,
        updateStepId = UpdatePropertyDetailsStepId.UpdateDetails,
        updateEntityId = propertyOwnershipId.toString(),
    ) {
    init {
        initializeJourneyDataIfNotInitialized()
    }

    override fun createOriginalJourneyData(): JourneyData {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)

        return mapOf(
            UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment to mapOf("ownershipType" to propertyOwnership.ownershipType),
            UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment to
                mapOf("numberOfHouseholds" to propertyOwnership.currentNumHouseholds),
            UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment to
                mapOf("numberOfPeople" to propertyOwnership.currentNumTenants),
        )
    }

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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds, null) },
            saveAfterSubmit = false,
        )

    private val numberOfHouseholdsStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds,
            page =
                Page(
                    formModel = NumberOfHouseholdsFormModel::class,
                    templateName = "forms/numberOfHouseholdsForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to "forms.update.numberOfHouseholds.fieldSetHeading",
                            "label" to "forms.numberOfHouseholds.label",
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateNumberOfPeople, null) },
            saveAfterSubmit = false,
        )

    private val numberOfPeopleStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateNumberOfPeople,
            page =
                Page(
                    formModel = NumberOfPeopleFormModel::class,
                    templateName = "forms/numberOfPeopleForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to "forms.update.numberOfPeople.fieldSetHeading",
                            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
                            "label" to "forms.numberOfPeople.label",
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateDetails, null) },
            saveAfterSubmit = false,
        )

    // The next action flow must have the `updateDetailsStep` after all data changing steps to ensure that validation for all of them is run
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(ownershipTypeStep, numberOfHouseholdsStep, numberOfPeopleStep, updateDetailsStep),
        )

    private fun updatePropertyAndRedirect(journeyData: JourneyData): String {
        val propertyUpdate =
            PropertyOwnershipUpdateModel(
                ownershipType = journeyData.getOwnershipTypeUpdateIfPresent(),
                numberOfHouseholds = journeyData.getNumberOfHouseholdsUpdateIfPresent(),
                numberOfPeople = journeyData.getNumberOfPeopleUpdateIfPresent(),
            )

        propertyOwnershipService.updatePropertyOwnership(propertyOwnershipId, propertyUpdate)

        journeyDataService.clearJourneyDataFromSession()

        return UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment
    }
}
