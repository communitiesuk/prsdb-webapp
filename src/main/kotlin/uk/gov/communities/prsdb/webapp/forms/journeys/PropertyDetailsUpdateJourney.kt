package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationNumberOfPeoplePage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getLatestNumberOfHouseholds
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOriginalIsOccupied
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.PropertyDetailsUpdateJourneyDataExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
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

        val licensingType = propertyOwnership.license?.licenseType ?: LicensingType.NO_LICENSING

        val originalPropertyData =
            mutableMapOf(
                UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment to mapOf("ownershipType" to propertyOwnership.ownershipType),
                UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment to mapOf("occupied" to propertyOwnership.isOccupied),
                UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment to
                    mapOf("numberOfHouseholds" to propertyOwnership.currentNumHouseholds),
                UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment to
                    mapOf(
                        "numberOfPeople" to propertyOwnership.currentNumTenants,
                        "numberOfHouseholds" to propertyOwnership.currentNumHouseholds,
                    ),
                UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment to mapOf("licensingType" to licensingType),
            )

        val licenceNumberUpdateStepId = PropertyDetailsUpdateJourneyDataExtensions.getLicenceNumberUpdateStepId(licensingType)
        licenceNumberUpdateStepId?.let {
            originalPropertyData[it.urlPathSegment] =
                mapOf("licenceNumber" to propertyOwnership.license?.licenseNumber!!)
        }

        return originalPropertyData
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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateLicensingType, null) },
            saveAfterSubmit = false,
        )

    private val licensingTypeStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateLicensingType,
            page =
                Page(
                    formModel = LicensingTypeFormModel::class,
                    templateName = "forms/licensingTypeForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to "forms.update.licensingType.fieldSetHeading",
                            "fieldSetHint" to "forms.licensingType.fieldSetHint",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = LicensingType.SELECTIVE_LICENCE,
                                        labelMsgKey = "forms.licensingType.radios.option.selectiveLicence.label",
                                        hintMsgKey = "forms.licensingType.radios.option.selectiveLicence.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = LicensingType.HMO_MANDATORY_LICENCE,
                                        labelMsgKey = "forms.licensingType.radios.option.hmoMandatory.label",
                                        hintMsgKey = "forms.licensingType.radios.option.hmoMandatory.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = LicensingType.HMO_ADDITIONAL_LICENCE,
                                        labelMsgKey = "forms.licensingType.radios.option.hmoAdditional.label",
                                        hintMsgKey = "forms.licensingType.radios.option.hmoAdditional.hint",
                                    ),
                                    RadiosDividerViewModel("forms.radios.dividerText"),
                                    RadiosButtonViewModel(
                                        value = LicensingType.NO_LICENSING,
                                        labelMsgKey = "forms.licensingType.radios.option.noLicensing.label",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { journeyData, _ -> licensingTypeHandleSubmitAndRedirect(journeyData) },
            nextAction = { journeyData, _ -> licensingTypeNextAction(journeyData) },
            saveAfterSubmit = false,
        )

    private val selectiveLicenceStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateSelectiveLicence,
            page =
                Page(
                    formModel = SelectiveLicenceFormModel::class,
                    templateName = "forms/licenceNumberForm",
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "fieldSetHeading" to "forms.selectiveLicence.fieldSetHeading",
                            "label" to "forms.selectiveLicence.label",
                            "detailSummary" to "forms.selectiveLicence.detail.summary",
                            "detailMainText" to "forms.selectiveLicence.detail.text",
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateOccupancy, null) },
            saveAfterSubmit = false,
        )

    private val hmoMandatoryLicenceStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence,
            page =
                Page(
                    formModel = HmoMandatoryLicenceFormModel::class,
                    templateName = "forms/licenceNumberForm",
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "fieldSetHeading" to "forms.hmoMandatoryLicence.fieldSetHeading",
                            "label" to "forms.hmoMandatoryLicence.label",
                            "detailSummary" to "forms.hmoMandatoryLicence.detail.summary",
                            "detailMainText" to "forms.hmoMandatoryLicence.detail.paragraph.one",
                            "detailAdditionalContent" to
                                mapOf(
                                    "bulletOne" to "forms.hmoMandatoryLicence.detail.bullet.one",
                                    "bulletTwo" to "forms.hmoMandatoryLicence.detail.bullet.two",
                                    "text" to "forms.hmoMandatoryLicence.detail.paragraph.two",
                                ),
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateOccupancy, null) },
            saveAfterSubmit = false,
        )

    private val hmoAdditionalLicenceStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence,
            page =
                Page(
                    formModel = HmoAdditionalLicenceFormModel::class,
                    templateName = "forms/licenceNumberForm",
                    content =
                        mapOf(
                            "title" to "registerProperty.title",
                            "fieldSetHeading" to "forms.hmoAdditionalLicence.fieldSetHeading",
                            "label" to "forms.hmoAdditionalLicence.label",
                            "detailSummary" to "forms.hmoAdditionalLicence.detail.summary",
                            "detailMainText" to "forms.hmoAdditionalLicence.detail.text",
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateOccupancy, null) },
            saveAfterSubmit = false,
        )

    private val occupancyStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateOccupancy,
            page =
                Page(
                    formModel = OccupancyFormModel::class,
                    templateName = "forms/propertyOccupancyForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to getOccupancyStepFieldSetHeading(),
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        valueStr = "yes",
                                        labelMsgKey = "forms.radios.option.yes.label",
                                        hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.radios.option.no.label",
                                        hintMsgKey = "forms.occupancy.radios.option.no.hint",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            nextAction = { journeyData, _ -> occupancyNextAction(journeyData) },
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
                            "fieldSetHeading" to getNumberOfHouseholdsStepFieldSetHeading(),
                            "label" to "forms.numberOfHouseholds.label",
                            BACK_URL_ATTR_NAME to getNumberOfHouseholdsStepBackUrl(),
                        ),
                ),
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateNumberOfPeople, null) },
            saveAfterSubmit = false,
        )

    private val numberOfPeopleStep =
        Step(
            id = UpdatePropertyDetailsStepId.UpdateNumberOfPeople,
            page =
                PropertyRegistrationNumberOfPeoplePage(
                    formModel = NumberOfPeopleFormModel::class,
                    templateName = "forms/numberOfPeopleForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to getNumberOfPeopleStepFieldSetHeading(),
                            "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
                            "label" to "forms.numberOfPeople.label",
                            BACK_URL_ATTR_NAME to getNumberOfPeopleStepBackUrl(),
                        ),
                    latestNumberOfHouseholds =
                        journeyDataService.getJourneyDataFromSession().getLatestNumberOfHouseholds(originalDataKey),
                ),
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateDetails, null) },
            saveAfterSubmit = false,
        )

    // The next action flow must have the `updateDetailsStep` after all data changing steps to ensure that validation for all of them is run
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                ownershipTypeStep,
                licensingTypeStep,
                selectiveLicenceStep,
                hmoMandatoryLicenceStep,
                hmoAdditionalLicenceStep,
                occupancyStep,
                numberOfHouseholdsStep,
                numberOfPeopleStep,
                updateDetailsStep,
            ),
        )

    private fun getOccupancyStepFieldSetHeading() =
        if (wasPropertyOriginallyOccupied()) {
            "forms.update.occupancy.occupied.fieldSetHeading"
        } else {
            "forms.occupancy.fieldSetHeading"
        }

    private fun getNumberOfHouseholdsStepFieldSetHeading() =
        if (wasPropertyOriginallyOccupied()) {
            "forms.update.numberOfHouseholds.fieldSetHeading"
        } else {
            "forms.numberOfHouseholds.fieldSetHeading"
        }

    private fun getNumberOfPeopleStepFieldSetHeading() =
        if (wasPropertyOriginallyOccupied()) {
            "forms.update.numberOfPeople.fieldSetHeading"
        } else {
            "forms.numberOfPeople.fieldSetHeading"
        }

    private fun getNumberOfHouseholdsStepBackUrl() =
        if (hasPropertyOccupancyBeenUpdated()) {
            UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment
        } else {
            UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment
        }

    private fun getNumberOfPeopleStepBackUrl() =
        if (hasPropertyOccupancyBeenUpdated()) {
            UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment
        } else {
            UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment
        }

    private fun occupancyNextAction(journeyData: JourneyData) =
        if (journeyData.getIsOccupiedUpdateIfPresent()!!) {
            Pair(UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds, null)
        } else {
            Pair(UpdatePropertyDetailsStepId.UpdateDetails, null)
        }

    private fun updatePropertyAndRedirect(journeyData: JourneyData): String {
        val propertyUpdate =
            PropertyOwnershipUpdateModel(
                ownershipType = journeyData.getOwnershipTypeUpdateIfPresent(),
                numberOfHouseholds = journeyData.getNumberOfHouseholdsUpdateIfPresent(),
                numberOfPeople = journeyData.getNumberOfPeopleUpdateIfPresent(),
                licensingType = journeyData.getLicensingTypeUpdateIfPresent(),
                licenceNumber = journeyData.getLicenceNumberUpdateIfPresent(originalDataKey),
            )

        propertyOwnershipService.updatePropertyOwnership(propertyOwnershipId, propertyUpdate)

        journeyDataService.clearJourneyDataFromSession()

        return UpdatePropertyDetailsStepId.UpdateDetails.urlPathSegment
    }

    private fun wasPropertyOriginallyOccupied() = journeyDataService.getJourneyDataFromSession().getOriginalIsOccupied(originalDataKey)!!

    private fun hasPropertyOccupancyBeenUpdated() = journeyDataService.getJourneyDataFromSession().getIsOccupiedUpdateIfPresent() != null

    private fun licensingTypeNextAction(journeyData: JourneyData): Pair<UpdatePropertyDetailsStepId, Int?> {
        val licensingType = journeyData.getLicensingTypeUpdateIfPresent()!!

        val nextActionStepId =
            PropertyDetailsUpdateJourneyDataExtensions.getLicenceNumberUpdateStepId(licensingType)
                ?: UpdatePropertyDetailsStepId.UpdateOccupancy

        return Pair(nextActionStepId, null)
    }

    private fun licensingTypeHandleSubmitAndRedirect(journeyData: JourneyData): String {
        val licensingType = journeyData.getLicensingTypeUpdateIfPresent()!!

        val redirectStepId =
            PropertyDetailsUpdateJourneyDataExtensions.getLicenceNumberUpdateStepId(licensingType)
                ?: UpdatePropertyDetailsStepId.UpdateDetails

        return redirectStepId.urlPathSegment
    }
}
