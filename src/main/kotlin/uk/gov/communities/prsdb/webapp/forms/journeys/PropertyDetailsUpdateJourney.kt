package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyDetailsUpdateJourneyFactory.Companion.getJourneyDataKey
import uk.gov.communities.prsdb.webapp.forms.pages.CheckLicensingAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.CheckOccupancyAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyDetailsUpdateJourneyStepFactory
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedJourneyExtensions.Companion.withBackUrlIfNotNullAndNotChangingAnswer
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberStepIdAndFormModel
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicenceNumberUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLicensingTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfHouseholdsUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getNumberOfPeopleUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOwnershipTypeUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyOwnershipUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
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
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import kotlin.reflect.KFunction

class PropertyDetailsUpdateJourney(
    validator: Validator,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipId: Long,
    stepName: String,
    isChangingAnswer: Boolean,
) : GroupedUpdateJourney<UpdatePropertyDetailsStepId>(
        journeyType = JourneyType.PROPERTY_DETAILS_UPDATE,
        initialStepId = UpdatePropertyDetailsStepId.UpdateOwnershipType,
        validator = validator,
        journeyDataService = journeyDataServiceFactory.create(getJourneyDataKey(propertyOwnershipId, stepName)),
        stepName = stepName,
        isChangingAnswer = isChangingAnswer,
    ) {
    override val stepRouter = GroupedUpdateStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)

    private val stepFactory =
        PropertyDetailsUpdateJourneyStepFactory(
            stepName,
            isChangingAnswer,
            RELATIVE_PROPERTY_DETAILS_PATH,
            journeyDataService,
        )

    init {
        initializeOriginalJourneyDataIfNotInitialized()
        initializeJourneyDataForSkippedStepsIfNotInitialized()
    }

    override fun createOriginalJourneyData(): JourneyData {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)

        infix fun <T : FormModel> StepId.toPageData(fromPropOwnershipFunc: KFunction<T>): Pair<String, PageData> =
            this.urlPathSegment to fromPropOwnershipFunc.call(propertyOwnership).toPageData()

        val originalPropertyData =
            mutableMapOf(
                UpdatePropertyDetailsStepId.UpdateOwnershipType toPageData OwnershipTypeFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.UpdateLicensingType toPageData LicensingTypeFormModel::fromPropertyOwnership,
                UpdatePropertyDetailsStepId.CheckYourLicensingAnswers.urlPathSegment to emptyMap<String, Any>(),
                stepFactory.occupancyStepId toPageData OccupancyFormModel::fromPropertyOwnership,
                stepFactory.numberOfHouseholdsStepId toPageData NumberOfHouseholdsFormModel::fromPropertyOwnership,
                stepFactory.numberOfPeopleStepId toPageData NumberOfPeopleFormModel::fromPropertyOwnership,
                stepFactory.checkOccupancyAnswersStepId.urlPathSegment to CheckAnswersFormModel().toPageData(),
            )

        val licenceNumberStepIdAndFormModel = propertyOwnership.getLicenceNumberStepIdAndFormModel()
        if (licenceNumberStepIdAndFormModel != null) {
            val (licenceNumberUpdateStepId, licenceFormModel) = licenceNumberStepIdAndFormModel
            originalPropertyData[licenceNumberUpdateStepId.urlPathSegment] = licenceFormModel.toPageData()
        }

        return originalPropertyData
    }

    private fun initializeJourneyDataForSkippedStepsIfNotInitialized() {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val originalJourneyData = JourneyDataHelper.getPageData(journeyData, originalDataKey) ?: return

        val journeyDataWithSkippedStepData =
            journeyData +
                stepFactory.skippedStepIds.mapNotNull { getOriginalDataPairForStepIfNotInitialized(it, originalJourneyData, journeyData) }
        journeyDataService.setJourneyDataInSession(journeyDataWithSkippedStepData)
    }

    private fun getOriginalDataPairForStepIfNotInitialized(
        stepId: UpdatePropertyDetailsStepId,
        originalJourneyData: JourneyData,
        journeyData: JourneyData,
    ): Pair<String, Any?>? {
        val stepUrlPathSegment = stepId.urlPathSegment
        return if (!journeyData.containsKey(stepUrlPathSegment)) {
            (stepUrlPathSegment to originalJourneyData[stepUrlPathSegment])
        } else {
            null
        }
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
            handleSubmitAndRedirect = { _, _, _ -> updatePropertyAndRedirect() },
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
                        ).withBackUrlIfNotNullAndNotChangingAnswer(RELATIVE_PROPERTY_DETAILS_PATH, isChangingAnswer),
                ),
            nextAction = { filteredJourneyData, _ -> licensingTypeNextAction(filteredJourneyData) },
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
                        ),
                ),
            nextAction = { _, _ -> Pair(UpdatePropertyDetailsStepId.CheckYourLicensingAnswers, null) },
            saveAfterSubmit = false,
        )

    private val checkLicensingAnswers =
        Step(
            id = UpdatePropertyDetailsStepId.CheckYourLicensingAnswers,
            page = CheckLicensingAnswersPage(journeyDataService),
            nextAction = { _, _ -> Pair(stepFactory.occupancyStepId, null) },
            handleSubmitAndRedirect = { _, _, _ -> updatePropertyAndRedirect() },
        )

    private val occupancyStep = stepFactory.createOccupancyStep()

    private val numberOfHouseholdsStep = stepFactory.createNumberOfHouseholdsStep()

    private val numberOfPeopleStep = stepFactory.createNumberOfPeopleStep()

    private val checkOccupancyAnswers =
        Step(
            id = stepFactory.checkOccupancyAnswersStepId,
            page = CheckOccupancyAnswersPage(stepFactory.stepGroupId, journeyDataService),
            handleSubmitAndRedirect = { _, _, _ -> updatePropertyAndRedirect() },
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

    private fun licensingTypeNextAction(filteredJourneyData: JourneyData): Pair<UpdatePropertyDetailsStepId, Int?> {
        val licensingType = filteredJourneyData.getLicensingTypeUpdateIfPresent()!!

        val nextActionStepId =
            PropertyDetailsUpdateJourneyExtensions.getLicenceNumberUpdateStepId(licensingType)
                ?: UpdatePropertyDetailsStepId.CheckYourLicensingAnswers

        return Pair(nextActionStepId, null)
    }

    private fun updatePropertyAndRedirect(): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val propertyUpdate =
            PropertyOwnershipUpdateModel(
                ownershipType = journeyData.getOwnershipTypeUpdateIfPresent(),
                licensingType = journeyData.getLicensingTypeUpdateIfPresent(),
                licenceNumber = journeyData.getLicenceNumberUpdateIfPresent(),
                numberOfHouseholds = journeyData.getNumberOfHouseholdsUpdateIfPresent(stepFactory.stepGroupId),
                numberOfPeople = journeyData.getNumberOfPeopleUpdateIfPresent(stepFactory.stepGroupId),
            )

        propertyOwnershipService.updatePropertyOwnership(propertyOwnershipId, propertyUpdate)

        clearRelatedJourneyContext()

        return RELATIVE_PROPERTY_DETAILS_PATH
    }

    private fun clearRelatedJourneyContext() {
        stepFactory.stepGroupId.relatedGroups.forEach {
            val groupJourneyDataKey = getJourneyDataKey(propertyOwnershipId, it)
            val groupJourneyDataService = journeyDataServiceFactory.create(groupJourneyDataKey)
            groupJourneyDataService.removeJourneyDataAndContextIdFromSession()
        }
    }

    companion object {
        // The path for the update journey is "{propertyDetailsPath}/update/{pathSegment}". As there is no trailing slash, any relative path is
        // relative to ".../update/". Therefore, the relative path to the "{propertyDetailsPath}" is just the parent of the current path.
        private const val RELATIVE_PROPERTY_DETAILS_PATH = ".."
    }
}
