package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toJavaLocalDate
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney.Companion.getAutomatchedEpc
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyComplianceJourney.Companion.updateEpcDetailsInSessionAndReturnUpdatedJourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.CheckUpdateGasSafetyAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertEngineerNum
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionOtherReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertIssueDate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getGasSafetyCertOriginalName
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasNewGasSafetyCertificate
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getStillHasNoCertOrExemption
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
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
    epcCertificateUrlProvider: EpcCertificateUrlProvider,
    checkingAnswersForStep: String?,
) : GroupedUpdateJourney<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE_UPDATE,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
        stepName = stepName,
    ) {
    init {
        initializeOriginalJourneyDataIfNotInitialized()
    }

    override val stepRouter = GroupedUpdateStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)

    private val checkingAnswersFor = PropertyComplianceStepId.entries.find { it.urlPathSegment == checkingAnswersForStep }

    override fun createOriginalJourneyData(): JourneyData {
        val factory =
            PropertyComplianceOriginalJourneyDataFactory(
                propertyComplianceService,
                propertyOwnershipId,
            )
        return factory.originalJourneyData
    }

    private val propertyComplianceSharedStepFactory =
        PropertyComplianceSharedStepFactory(
            defaultSaveAfterSubmit = false,
            nextActionAfterGasSafetyTask = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
            nextActionAfterEicrTask = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
            nextActionAfterEpcTask = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
            isCheckingOrUpdatingAnswers = true,
            journeyDataService = journeyDataService,
            epcCertificateUrlProvider = epcCertificateUrlProvider,
        )

    override val sections: List<JourneySection<PropertyComplianceStepId>> =
        listOf(
            JourneySection(
                listOf(
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
                    propertyComplianceSharedStepFactory.createGasSafetyIssueDateStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyEngineerNumStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyUploadStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyUploadConfirmationStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyOutdatedStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionOtherReasonStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createGasSafetyExemptionMissingStep(),
                    gasSafetyCheckYourAnswersStep,
                ),
            )

    private val eicrTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.UpdateEICR,
                setOf(
                    updateEICRStep,
                    propertyComplianceSharedStepFactory.createEicrIssueDateStep(),
                    propertyComplianceSharedStepFactory.createEicrUploadStep(),
                    propertyComplianceSharedStepFactory.createEicrUploadConfirmationStep(),
                    propertyComplianceSharedStepFactory.createEicrOutdatedStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionOtherReasonStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createEicrExemptionMissingStep(),
                    eicrCheckYourAnswersStep,
                ),
            )

    private val epcLookupStep
        get() =
            propertyComplianceSharedStepFactory.createEpcLookupStep(
                handleSubmitAndRedirect = { filteredJourneyData ->
                    epcLookupStepHandleSubmitAndRedirect(filteredJourneyData)
                },
            )

    private val checkMatchedEpcStep
        get() =
            propertyComplianceSharedStepFactory.createCheckMatchedEpcStep(
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
                    propertyComplianceSharedStepFactory.createEpcNotAutoMatchedStep(),
                    propertyComplianceSharedStepFactory.createCheckAutoMatchedEpcStep(),
                    epcLookupStep,
                    checkMatchedEpcStep,
                    propertyComplianceSharedStepFactory.createEpcNotFoundStep(),
                    propertyComplianceSharedStepFactory.createEpcSupersededStep(
                        handleSubmitAndRedirect = { filteredJourneyData ->
                            epcSupersededHandleSubmitAndRedirect(filteredJourneyData)
                        },
                    ),
                    propertyComplianceSharedStepFactory.createEpcExpiryCheckStep(),
                    propertyComplianceSharedStepFactory.createEpcExpiredStep(),
                    propertyComplianceSharedStepFactory.createEpcMissingStep(),
                    propertyComplianceSharedStepFactory.createEpcExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createEpcExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionCheckStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionReasonStep(),
                    propertyComplianceSharedStepFactory.createMeesExemptionConfirmationStep(),
                    propertyComplianceSharedStepFactory.createLowEnergyRatingStep(),
                    epcCheckYourAnswersStep,
                ),
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
                                BACK_URL_ATTR_NAME to
                                    PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
                            ),
                    ),
                nextAction = { journeyData, _ ->
                    if (journeyData.getHasNewGasSafetyCertificate()!!) {
                        Pair(PropertyComplianceStepId.GasSafetyIssueDate, null)
                    } else if (journeyData.getStillHasNoCertOrExemption() ?: false) {
                        Pair(PropertyComplianceStepId.GasSafetyExemptionMissing, null)
                    } else {
                        Pair(PropertyComplianceStepId.GasSafetyExemptionReason, null)
                    }
                },
                saveAfterSubmit = false,
            )

    private val gasSafetyCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                page = CheckUpdateGasSafetyAnswersPage(journeyDataService),
                saveAfterSubmit = false,
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
                handleSubmitAndRedirect = { _, _, _ ->
                    updateComplianceAndRedirect()
                },
            )

    // TODO PRSD-1246: Implement new EICR or exemption step
    private val updateEICRStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEICR,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1246: Implement new EICR or exemption step"),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrExemptionReason, null) },
                saveAfterSubmit = false,
            )

    // TODO: PRSD-1247: Implement EICR check your answers step
    private val eicrCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1247:: Implement EICR Check Your Answers step"),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
                saveAfterSubmit = false,
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
                                BACK_URL_ATTR_NAME to
                                    PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId),
                            ),
                    ),
                nextAction = { filteredJourneyData, _ -> updateEpcStepNextAction(filteredJourneyData) },
                handleSubmitAndRedirect = { filteredJourneyData, _, _ -> updateEpcStepHandleSubmitAndRedirect(filteredJourneyData) },
                saveAfterSubmit = false,
            )

    // TODO PRSD-1313: Implement EPC check your answers step
    private val epcCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1313: Implement EPC Check Your Answers step"),
                    ),
                saveAfterSubmit = false,
            )

    private fun updateEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId, Int?> =
        if (filteredJourneyData.getHasNewEPC()) {
            if (filteredJourneyData.getEpcDetails(autoMatched = true) != null) {
                Pair(PropertyComplianceStepId.CheckAutoMatchedEpc, null)
            } else {
                Pair(PropertyComplianceStepId.EpcNotAutoMatched, null)
            }
        } else {
            Pair(PropertyComplianceStepId.EpcExemptionReason, null)
        }

    private fun updateEpcStepHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        if (filteredJourneyData.getHasNewEPC()) {
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
        val nextAction = propertyComplianceSharedStepFactory.checkMatchedEpcStepNextAction(filteredJourneyData)
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

    // TODO 1247, 1313 - add this as the handleSubmitAndRedirect method and test
    private fun updateComplianceAndRedirect(): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val gasSafetyUpdate = createGasSafetyUpdateOrNull(journeyData, propertyOwnershipId)
        // TODO PRSD-1247: Add EICR updates from journeyData to complianceUpdate
        // TODO PRSD-1313: Add EPC updates from journeyData to complianceUpdate
        val complianceUpdate = PropertyComplianceUpdateModel(gasSafetyUpdate)

        propertyComplianceService.updatePropertyCompliance(propertyOwnershipId, complianceUpdate)

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

    companion object {
        val initialStepId = PropertyComplianceStepId.UpdateGasSafety
    }
}
