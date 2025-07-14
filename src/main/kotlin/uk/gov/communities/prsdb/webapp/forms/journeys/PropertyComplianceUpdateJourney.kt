package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.factories.PropertyComplianceSharedStepFactory
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.JourneyContextHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

class PropertyComplianceUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    stepName: String,
    private val propertyOwnershipId: Long,
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
        isCheckingAnswers = JourneyContextHelper.isCheckingAnswers(checkingAnswersForStep),
    ) {
    init {
        initializeOriginalJourneyDataIfNotInitialized()
    }

    override val stepRouter = GroupedUpdateStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)

    private val checkingAnswersFor = PropertyComplianceStepId.entries.find { it.urlPathSegment == checkingAnswersForStep }

    override fun createOriginalJourneyData(): JourneyData {
        val propertyCompliance = propertyComplianceService.getComplianceForProperty(propertyOwnershipId)

        // TODO PRSD-1244: Add original gas safety step data
        // TODO PRSD-1246: Add original EICR step data
        // TODO: PRSD-1312: Add original EPC step data
        val originalJourneyData = emptyMap<String, Any>()

        return originalJourneyData
    }

    private val propertyComplianceSharedStepFactory =
        PropertyComplianceSharedStepFactory(
            defaultSaveAfterSubmit = false,
            nextActionAfterGasSafetyTask = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
            nextActionAfterEicrTask = PropertyComplianceStepId.UpdateEicrCheckYourAnswers,
            nextActionAfterEpcTask = PropertyComplianceStepId.UpdateEpcCheckYourAnswers,
            isCheckingAnswers = isCheckingAnswers,
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

    private val epcLookupStep =
        propertyComplianceSharedStepFactory.createGetEpcLookupStep(
            handleSubmitAndRedirect = { filteredJourneyData ->
                epcLookupStepHandleSubmitAndRedirect(filteredJourneyData)
            },
        )

    private val checkMatchedEpcStep =
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

    // TODO PRSD-1244: Implement gas safety step
    private val updateGasSafetyStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateGasSafety,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1244: Implement gas safety step"),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.GasSafetyIssueDate, null) },
                saveAfterSubmit = false,
            )

    // TODO PRSD-1245: Implement gas safety check your answers step
    private val gasSafetyCheckYourAnswersStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1245: Implement gas safety Check Your Answers step"),
                    ),
                saveAfterSubmit = false,
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
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
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrIssueDate, null) },
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

    // TODO PRSD-1312: Implement new epc or exemption step
    private val updateEPCStep
        get() =
            Step(
                id = PropertyComplianceStepId.UpdateEpc,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/todo",
                        content =
                            mapOf("todoComment" to "TODO PRSD-1312: Implement new EPC or exemption step"),
                    ),
                // For PRSD-1312 - need search by uprn as part of handleSubmitAndRedirect
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EpcNotAutoMatched, null) },
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
                            mapOf("todoComment" to "TODO PRSD-1313:: Implement EICR Check Your Answers step"),
                    ),
                saveAfterSubmit = false,
            )

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

    // TODO PRSD-1245, 1247, 1313 - add this as the handleSubmitAndRedirect method and test
    private fun updateComplianceAndRedirect(): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        // TODO PRSD-1245: Add gas safety updates from journeyData to complianceUpdate
        // TODO PRSD-1247: Add EICR updates from journeyData to complianceUpdate
        // TODO PRSD-1313: Add EPC updates from journeyData to complianceUpdate
        val complianceUpdate = PropertyComplianceUpdateModel()

        propertyComplianceService.updatePropertyCompliance(propertyOwnershipId, complianceUpdate)

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
    }

    companion object {
        val initialStepId = PropertyComplianceStepId.UpdateGasSafety
    }
}
