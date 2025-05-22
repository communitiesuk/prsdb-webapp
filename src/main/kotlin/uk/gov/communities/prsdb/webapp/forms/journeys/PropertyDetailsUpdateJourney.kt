package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.pages.CheckLicensingAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.CheckOccupancyAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationNumberOfPeoplePage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLatestNumberOfHouseholds
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberStepIdAndFormModel
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOriginalIsOccupied
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import kotlin.reflect.KFunction

class PropertyDetailsUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipId: Long,
    stepName: String,
) : GroupedUpdateJourney<UpdatePropertyDetailsStepId>(
        journeyType = JourneyType.PROPERTY_DETAILS_UPDATE,
        initialStepId = UpdatePropertyDetailsStepId.UpdateOwnershipType,
        validator = validator,
        journeyDataService = journeyDataService,
        stepName = stepName,
    ) {
    init {
        initializeJourneyDataIfNotInitialized()
    }

    override val stepRouter = GroupedStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)

    override fun createOriginalJourneyData(): JourneyData {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)

        infix fun <T : FormModel> StepId.toPageData(fromPropOwnershipFunc: KFunction<T>): Pair<String, PageData> =
            this.urlPathSegment to fromPropOwnershipFunc.call(propertyOwnership).toPageData()

        val originalPropertyData =
            mutableMapOf(
                UpdatePropertyDetailsStepId.UpdateOwnershipType toPageData OwnershipTypeFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.UpdateOccupancy toPageData OccupancyFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds toPageData NumberOfHouseholdsFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.UpdateNumberOfPeople toPageData NumberOfPeopleFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers.urlPathSegment to mapOf<String, Any>() as PageData,
                UpdatePropertyDetailsStepId.UpdateLicensingType toPageData LicensingTypeFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.CheckYourLicensingAnswers.urlPathSegment to mapOf<String, Any>() as PageData,
            )

        val licenceNumberStepIdAndFormModel = propertyOwnership.getLicenceNumberStepIdAndFormModel()
        if (licenceNumberStepIdAndFormModel != null) {
            val (licenceNumberUpdateStepId, licenceFormModel) = licenceNumberStepIdAndFormModel
            originalPropertyData[licenceNumberUpdateStepId.urlPathSegment] = licenceFormModel.toPageData()
        }

        return originalPropertyData
    }

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
                            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                            "showWarning" to true,
                            BACK_URL_ATTR_NAME to RELATIVE_PROPERTY_DETAILS_PATH,
                        ),
                ),
            handleSubmitAndRedirect = { journeyData, _, _ -> updatePropertyAndRedirect(journeyData) },
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
                            BACK_URL_ATTR_NAME to RELATIVE_PROPERTY_DETAILS_PATH,
                        ),
                ),
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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.CheckYourLicensingAnswers, null) },
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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.CheckYourLicensingAnswers, null) },
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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.CheckYourLicensingAnswers, null) },
            saveAfterSubmit = false,
        )

    private val checkLicensingAnswers =
        Step(
            id = UpdatePropertyDetailsStepId.CheckYourLicensingAnswers,
            page = CheckLicensingAnswersPage(),
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.UpdateOccupancy, null) },
            handleSubmitAndRedirect = { journeyData, _, _ -> updatePropertyAndRedirect(journeyData) },
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
                            BACK_URL_ATTR_NAME to RELATIVE_PROPERTY_DETAILS_PATH,
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
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers, null) },
            saveAfterSubmit = false,
        )

    private val checkOccupancyAnswers =
        Step(
            id = UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers,
            page = CheckOccupancyAnswersPage(),
            handleSubmitAndRedirect = { journeyData, _, _ -> updatePropertyAndRedirect(journeyData) },
            saveAfterSubmit = false,
        )

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                ownershipTypeStep,
                licensingTypeStep,
                selectiveLicenceStep,
                hmoMandatoryLicenceStep,
                hmoAdditionalLicenceStep,
                checkLicensingAnswers,
                occupancyStep,
                numberOfHouseholdsStep,
                numberOfPeopleStep,
                checkOccupancyAnswers,
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
            RELATIVE_PROPERTY_DETAILS_PATH
        }

    private fun getNumberOfPeopleStepBackUrl() =
        if (hasNumberOfHouseholdsBeenUpdated()) {
            UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment
        } else {
            RELATIVE_PROPERTY_DETAILS_PATH
        }

    private fun licensingTypeNextAction(journeyData: JourneyData): Pair<UpdatePropertyDetailsStepId, Int?> {
        val licensingType = journeyData.getLicensingTypeUpdateIfPresent()!!

        val nextActionStepId =
            PropertyDetailsUpdateJourneyExtensions.getLicenceNumberUpdateStepId(licensingType)
                ?: UpdatePropertyDetailsStepId.CheckYourLicensingAnswers

        return Pair(nextActionStepId, null)
    }

    private fun occupancyNextAction(journeyData: JourneyData) =
        if (journeyData.getIsOccupiedUpdateIfPresent()!!) {
            Pair(UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds, null)
        } else {
            Pair(UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers, null)
        }

    private fun updatePropertyAndRedirect(journeyData: JourneyData): String {
        val propertyUpdate =
            PropertyOwnershipUpdateModel(
                ownershipType = journeyData.getOwnershipTypeUpdateIfPresent(),
                numberOfHouseholds = journeyData.getNumberOfHouseholdsUpdateIfPresent(),
                numberOfPeople = journeyData.getNumberOfPeopleUpdateIfPresent(),
                licensingType = journeyData.getLicensingTypeUpdateIfPresent(),
                licenceNumber = journeyData.getLicenceNumberUpdateIfPresent(),
            )

        propertyOwnershipService.updatePropertyOwnership(propertyOwnershipId, propertyUpdate)

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return RELATIVE_PROPERTY_DETAILS_PATH
    }

    private fun wasPropertyOriginallyOccupied() = journeyDataService.getJourneyDataFromSession().getOriginalIsOccupied(originalDataKey)!!

    private fun hasPropertyOccupancyBeenUpdated() = journeyDataService.getJourneyDataFromSession().getIsOccupiedUpdateIfPresent() != null

    private fun hasNumberOfHouseholdsBeenUpdated() =
        journeyDataService.getJourneyDataFromSession().getNumberOfHouseholdsUpdateIfPresent() != null

    companion object {
        // The path for the update journey is "{propertyDetailsPath}/update/{pathSegment}". As there is no trailing slash, any relative path is
        // relative to ".../update/". Therefore, the relative path to the "{propertyDetailsPath}" is just the parent of the current path.
        private const val RELATIVE_PROPERTY_DETAILS_PATH = ".."
    }
}
