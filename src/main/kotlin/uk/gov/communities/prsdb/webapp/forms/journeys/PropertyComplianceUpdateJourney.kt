package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toJavaLocalDate
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney.Companion.getAutomatchedEpc
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney.Companion.updateEpcDetailsInSessionAndReturnUpdatedJourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.CheckUpdateEicrAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.CheckUpdateEpcAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.CheckUpdateGasSafetyAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.UnvisitablePage
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedJourneyExtensions.Companion.withBackUrlIfNotNullAndNotCheckingAnswers
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEicrOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewGasSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getStillHasNoEicrOrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getStillHasNoEpcOrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getStillHasNoGasCertOrExemption
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateGasSafetyCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyComplianceUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    stepName: String,
    private val propertyOwnershipId: Long,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val epcLookupService: EpcLookupService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
    private val checkingAnswersForStep: String?,
) : GroupedUpdateJourney<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE_UPDATE,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
        stepName = stepName,
    ) {
    init {
        initializeOriginalJourneyDataIfNotInitialized()
        initializeJourneyDataForSkippedStepsIfNotInitialized()
    }

    override val stepRouter = GroupedUpdateStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)

    private val checkingAnswersFor = PropertyComplianceStepId.entries.find { it.urlPathSegment == checkingAnswersForStep }

    private val stepFactory
        get() =
            PropertyComplianceSharedStepFactory(
                defaultSaveAfterSubmit = false,
                isUpdateJourney = true,
                isCheckingOrUpdatingAnswers = true,
                journeyDataService = journeyDataService,
                epcCertificateUrlProvider = epcCertificateUrlProvider,
                stepName = stepName,
            )

    override fun createOriginalJourneyData(): JourneyData =
        propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)?.let {
            PropertyComplianceOriginalJourneyData.fromPropertyCompliance(it, stepFactory)
        } ?: emptyMap()

    // TODO PRSD-1392 - commonise this and the equivalent in PropertyDetailsUpdateJourney
    private fun initializeJourneyDataForSkippedStepsIfNotInitialized() {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val originalJourneyData = JourneyDataHelper.getPageData(journeyData, originalDataKey) ?: return

        val journeyDataWithSkippedStepData =
            journeyData +
                stepFactory.skippedStepIds.mapNotNull {
                    getOriginalDataPairForStepIfNotInitialized(it, originalJourneyData, journeyData)
                } +
                stepFactory.skippedNonStepJourneyDataKeys.mapNotNull { key ->
                    originalJourneyData[key]?.let { value -> key to value }
                }

        journeyDataService.setJourneyDataInSession(journeyDataWithSkippedStepData)
    }

    private fun getOriginalDataPairForStepIfNotInitialized(
        stepId: PropertyComplianceStepId,
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

    override val sections: List<JourneySection<PropertyComplianceStepId>> =
        listOf(
            JourneySection(
                listOf(
                    JourneyTask.withOneStep(checkComplianceExistsStep),
                    gasSafetyTask,
                    eicrTask,
                    epcTask,
                ),
            ),
        )

    private val gasSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.UpdateGasSafety,
                setOf(
                    updateGasSafetyStep,
                    stepFactory.createGasSafetyIssueDateStep(),
                    stepFactory.createGasSafetyEngineerNumStep(),
                    stepFactory.createGasSafetyUploadStep(),
                    stepFactory.createGasSafetyUploadConfirmationStep(),
                    stepFactory.createGasSafetyOutdatedStep(),
                    stepFactory.createGasSafetyExemptionStep(),
                    stepFactory.createGasSafetyExemptionReasonStep(),
                    stepFactory.createGasSafetyExemptionOtherReasonStep(),
                    stepFactory.createGasSafetyExemptionConfirmationStep(),
                    stepFactory.createGasSafetyExemptionMissingStep(),
                    gasSafetyCheckYourAnswersStep,
                ),
            )

    private val eicrTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.UpdateEICR,
                setOf(
                    updateEICRStep,
                    stepFactory.createEicrIssueDateStep(),
                    stepFactory.createEicrUploadStep(),
                    stepFactory.createEicrUploadConfirmationStep(),
                    stepFactory.createEicrOutdatedStep(),
                    stepFactory.createEicrExemptionStep(),
                    stepFactory.createEicrExemptionReasonStep(),
                    stepFactory.createEicrExemptionOtherReasonStep(),
                    stepFactory.createEicrExemptionConfirmationStep(),
                    stepFactory.createEicrExemptionMissingStep(),
                    eicrCheckYourAnswersStep,
                ),
            )

    private val epcLookupStep
        get() =
            stepFactory.createEpcLookupStep(
                handleSubmitAndRedirect = { filteredJourneyData ->
                    epcLookupStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private val checkMatchedEpcStep
        get() =
            stepFactory.createCheckMatchedEpcStep(
                handleSubmitAndRedirect = { filteredJourneyData ->
                    checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private val epcTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.UpdateEpc,
                setOf(
                    updateEPCStep,
                    stepFactory.createEpcNotAutoMatchedStep(),
                    stepFactory.createCheckAutoMatchedEpcStep(),
                    epcLookupStep,
                    checkMatchedEpcStep,
                    stepFactory.createEpcNotFoundStep(),
                    stepFactory.createEpcSupersededStep(
                        handleSubmitAndRedirect = { filteredJourneyData ->
                            epcSupersededHandleSubmitAndRedirect(filteredJourneyData)
                        },
                    ),
                    stepFactory.createEpcExpiryCheckStep(),
                    stepFactory.createEpcExpiredStep(),
                    stepFactory.createEpcMissingStep(),
                    stepFactory.createEpcExemptionReasonStep(),
                    stepFactory.createEpcExemptionConfirmationStep(),
                    stepFactory.createMeesExemptionCheckStep(),
                    stepFactory.createMeesExemptionReasonStep(),
                    stepFactory.createMeesExemptionConfirmationStep(),
                    stepFactory.createLowEnergyRatingStep(),
                    epcCheckYourAnswersStep,
                    meesCheckYourAnswersStep,
                ),
            )

    private val checkComplianceExistsStep
        get() =
            Step(
                id = PropertyComplianceStepId.CheckComplianceExists,
                page = UnvisitablePage(errorMessage = "CheckComplianceExists should never be reached."),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.UpdateGasSafety, null) },
                saveAfterSubmit = false,
            )

    private val updateGasSafetyStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateGasSafety,
                page =
                    Page(
                        formModel = UpdateGasSafetyCertificateFormModel::class,
                        templateName = "forms/updateComplianceCertificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.update.gasSafetyType.fieldSetHeading",
                                "fieldSetHint" to "forms.gasSafety.fieldSetHint",
                                "radioVariableName" to UpdateGasSafetyCertificateFormModel::hasNewCertificate.name,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            labelMsgKey = "forms.update.gasSafetyType.certificate",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            labelMsgKey = "forms.update.gasSafetyType.exemption",
                                        ),
                                    ),
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(
                                PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
                                isCheckingAnswers = checkingAnswersForStep != null,
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> updateGasSafetyNextAction(filteredJourneyData) },
                saveAfterSubmit = false,
            )

    private val gasSafetyCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                page = CheckUpdateGasSafetyAnswersPage(journeyDataService, unreachableStepRedirect),
                saveAfterSubmit = false,
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ ->
                    updateComplianceAndRedirect(filteredJourneyData)
                },
            )

    private val updateEICRStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEICR,
                page =
                    Page(
                        formModel = UpdateEicrFormModel::class,
                        templateName = "forms/updateComplianceCertificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.update.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.eicr.fieldSetHint",
                                "radioVariableName" to UpdateEicrFormModel::hasNewCertificate.name,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            labelMsgKey = "forms.update.eicr.certificate",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            labelMsgKey = "forms.update.eicr.exemption",
                                        ),
                                    ),
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(
                                PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
                                isCheckingAnswers = checkingAnswersForStep != null,
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> updateEicrNextAction(filteredJourneyData) },
                saveAfterSubmit = false,
            )

    private val eicrCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
                page = CheckUpdateEicrAnswersPage(journeyDataService, unreachableStepRedirect),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
                saveAfterSubmit = false,
                handleSubmitAndRedirect = { filteredJourneyData, _, _ ->
                    updateComplianceAndRedirect(filteredJourneyData)
                },
            )

    private val updateEPCStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEpc,
                page =
                    Page(
                        formModel = UpdateEpcFormModel::class,
                        templateName = "forms/updateComplianceCertificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.update.epc.fieldSetHeading",
                                "fieldSetHint" to "forms.epc.fieldSetHint",
                                "radioVariableName" to UpdateEpcFormModel::hasNewCertificate.name,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            labelMsgKey = "forms.update.epc.certificate",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            labelMsgKey = "forms.update.epc.exemption",
                                        ),
                                    ),
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ).withBackUrlIfNotNullAndNotCheckingAnswers(
                                PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
                                isCheckingAnswers = checkingAnswersForStep != null,
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> updateEpcStepNextAction(filteredJourneyData) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> updateEpcStepHandleSubmitAndRedirect(filteredJourneyData) },
                saveAfterSubmit = false,
            )

    private fun updateGasSafetyNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId, Int?> =
        if (filteredJourneyData.getHasNewGasSafetyCertificate()!!) {
            Pair(PropertyComplianceStepId.GasSafetyIssueDate, null)
        } else if (filteredJourneyData.getStillHasNoGasCertOrExemption() ?: false) {
            Pair(PropertyComplianceStepId.GasSafetyExemptionMissing, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemptionReason, null)
        }

    private fun updateEicrNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasNewEICR()!!) {
            Pair(PropertyComplianceStepId.EicrIssueDate, null)
        } else if (filteredJourneyData.getStillHasNoEicrOrExemption() ?: false) {
            Pair(PropertyComplianceStepId.EicrExemptionMissing, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionReason, null)
        }

    private val epcCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
                page =
                    CheckUpdateEpcAnswersPage(
                        journeyDataService,
                        epcCertificateUrlProvider,
                        unreachableStepRedirect,
                        stepFactory,
                    ),
                saveAfterSubmit = false,
                handleSubmitAndRedirect = { filteredJourneyData, _, _ ->
                    updateComplianceAndRedirect(filteredJourneyData)
                },
            )

    // TODO PRSD-1392 - maybe commonise this with epcCheckYourAnswersStep in the stepFactory
    private val meesCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateMeesCheckYourAnswers,
                page =
                    CheckUpdateEpcAnswersPage(
                        journeyDataService,
                        epcCertificateUrlProvider,
                        unreachableStepRedirect,
                        stepFactory,
                    ),
                saveAfterSubmit = false,
                handleSubmitAndRedirect = { filteredJourneyData, _, _ ->
                    updateComplianceAndRedirect(filteredJourneyData)
                },
            )

    private fun updateEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId, Int?> =
        if (filteredJourneyData.getHasNewEPC()!!) {
            if (filteredJourneyData.getEpcDetails(autoMatched = true) != null) {
                Pair(stepFactory.checkAutoMatchedEpcStepId, null)
            } else {
                Pair(stepFactory.epcNotAutomatchedStepId, null)
            }
        } else if (filteredJourneyData.getStillHasNoEpcOrExemption() ?: false) {
            Pair(stepFactory.epcNotFoundStepId, null)
        } else {
            Pair(stepFactory.epcExemptionReasonStepId, null)
        }

    private fun updateEpcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        if (filteredJourneyData.getHasNewEPC()!!) {
            val epcDetails = getAutomatchedEpc(propertyOwnershipId, epcLookupService, propertyOwnershipService)

            val newFilteredJourneyData =
                updateEpcDetailsInSessionAndReturnUpdatedJourneyData(
                    journeyDataService,
                    filteredJourneyData,
                    epcDetails,
                    autoMatchedEpc = true,
                )
            return getRedirectForNextStep(updateEPCStep, newFilteredJourneyData, null, checkingAnswersFor)
        }
        return getRedirectForNextStep(updateEPCStep, filteredJourneyData, null, checkingAnswersFor)
    }

    private fun checkMatchedEpcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val nextAction = stepFactory.checkMatchedEpcStepNextAction(filteredJourneyData)
        val overriddenRedirectStepId =
            PropertyComplianceJourney.getRedirectStepOverrideForCheckMatchedEpcStepHandleSubmitAndRedirect(nextAction)

        return getRedirectForNextStep(checkMatchedEpcStep, filteredJourneyData, null, checkingAnswersFor, overriddenRedirectStepId)
    }

    private fun epcLookupStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val newFilteredJourneyData =
            PropertyComplianceJourney.epcLookupStepHandleSubmit(
                filteredJourneyData,
                journeyDataService,
                epcLookupService,
            )
        return getRedirectForNextStep(epcLookupStep, newFilteredJourneyData, null, checkingAnswersFor)
    }

    private fun epcSupersededHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        val newFilteredJourneyData =
            PropertyComplianceJourney.epcSupersededStepHandleSubmit(
                filteredJourneyData,
                journeyDataService,
                epcLookupService,
            )

        return getRedirectForNextStep(epcLookupStep, newFilteredJourneyData, null, checkingAnswersFor)
    }

    private fun updateComplianceAndRedirect(filteredJourneyData: JourneyData): String {
        val submittedJourneyData = journeyDataService.getJourneyDataFromSession()
        val relevantJourneyData = submittedJourneyData.filterKeys { it in filteredJourneyData.keys }

        val gasSafetyUpdate = createGasSafetyUpdateOrNull(relevantJourneyData, propertyOwnershipId)
        val eicrUpdate = createEicrUpdateOrNull(relevantJourneyData, propertyOwnershipId)
        val epcUpdate = createEpcUpdateOrNull(relevantJourneyData)
        val complianceUpdate = PropertyComplianceUpdateModel(gasSafetyUpdate, eicrUpdate, epcUpdate)

        propertyComplianceService.updatePropertyCompliance(propertyOwnershipId, complianceUpdate) {
            throwIfSubmittedDataIsAnInvalidUpdate(relevantJourneyData)
        }

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
    }

    fun createGasSafetyUpdateOrNull(
        journeyData: JourneyData,
        propertyOwnershipId: Long,
    ): GasSafetyCertUpdateModel? =
        journeyData.getHasNewGasSafetyCertificate()?.let { data ->
            GasSafetyCertUpdateModel(
                s3Key =
                    journeyData.getGasSafetyCertOriginalName()?.let {
                        PropertyComplianceJourneyHelper.getCertFilename(
                            propertyOwnershipId,
                            PropertyComplianceStepId.GasSafetyUpload.urlPathSegment,
                            it,
                        )
                    },
                issueDate = journeyData.getGasSafetyCertIssueDate()?.toJavaLocalDate(),
                engineerNum = journeyData.getGasSafetyCertEngineerNum(),
                exemptionReason = journeyData.getGasSafetyCertExemptionReason(),
                exemptionOtherReason = journeyData.getGasSafetyCertExemptionOtherReason(),
            )
        }

    fun createEicrUpdateOrNull(
        journeyData: JourneyData,
        propertyOwnershipId: Long,
    ): EicrUpdateModel? =
        journeyData.getHasNewEICR()?.let { data ->
            EicrUpdateModel(
                s3Key =
                    journeyData.getEicrOriginalName()?.let {
                        PropertyComplianceJourneyHelper.getCertFilename(
                            propertyOwnershipId,
                            PropertyComplianceStepId.EicrUpload.urlPathSegment,
                            it,
                        )
                    },
                issueDate = journeyData.getEicrIssueDate()?.toJavaLocalDate(),
                exemptionReason = journeyData.getEicrExemptionReason(),
                exemptionOtherReason = journeyData.getEicrExemptionOtherReason(),
            )
        }

    fun createEpcUpdateOrNull(journeyData: JourneyData): EpcUpdateModel? =
        journeyData.getHasNewEPC()?.let { data ->
            EpcUpdateModel(
                url =
                    journeyData
                        .getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId)
                        ?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
                expiryDate =
                    journeyData
                        .getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId)
                        ?.expiryDate
                        ?.toJavaLocalDate(),
                tenancyStartedBeforeExpiry =
                    journeyData.getDidTenancyStartBeforeEpcExpiry(stepFactory.epcExpiryCheckStepId),
                energyRating =
                    journeyData.getAcceptedEpcDetails(stepFactory.checkAutoMatchedEpcStepId)?.energyRating,
                exemptionReason = journeyData.getEpcExemptionReason(stepFactory.epcExemptionReasonStepId),
                meesExemptionReason = journeyData.getMeesExemptionReason(stepFactory.meesExemptionReasonStepId),
            )
        }

    companion object {
        val initialStepId = PropertyComplianceStepId.CheckComplianceExists
    }
}
