package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceSharedSteps
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

class PropertyComplianceUpdateJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    stepName: String,
    isCheckingAnswers: Boolean,
    private val propertyOwnershipId: Long,
    private val propertyComplianceService: PropertyComplianceService,
) : GroupedUpdateJourney<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE_UPDATE,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
        stepName = stepName,
        isCheckingAnswers = isCheckingAnswers,
    ) {
    init {
        initializeOriginalJourneyDataIfNotInitialized()
    }

    override val stepRouter = GroupedUpdateStepRouter(this)

    override val unreachableStepRedirect = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)

    override fun createOriginalJourneyData(): JourneyData {
        val propertyCompliance = propertyComplianceService.getComplianceForProperty(propertyOwnershipId)

        // TODO PRSD-1244: Add original gas safety step data
        val originalJourneyData = emptyMap<String, Any>()

        return originalJourneyData
    }

    override val sections: List<JourneySection<PropertyComplianceStepId>> =
        listOf(
            JourneySection(
                listOf(
                    gasSafetyTask,
                ),
            ),
        )

    private val gasSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.UpdateGasSafety,
                setOf(
                    updateGasSafetyStep,
                    PropertyComplianceSharedSteps.gasSafetyIssueDateStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyEngineerNumStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyUploadStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyUploadConfirmationStep(
                        PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                        isCheckingAnswers = true,
                        saveAfterSubmit = false,
                    ),
                    PropertyComplianceSharedSteps.gasSafetyOutdatedStep(
                        PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                        isCheckingAnswers = true,
                        saveAfterSubmit = false,
                    ),
                    PropertyComplianceSharedSteps.gasSafetyExemptionStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyExemptionReasonStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyExemptionOtherReasonStep(saveAfterSubmit = false),
                    PropertyComplianceSharedSteps.gasSafetyExemptionConfirmationStep(
                        PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                        isCheckingAnswers = true,
                        saveAfterSubmit = false,
                    ),
                    PropertyComplianceSharedSteps.gasSafetyExemptionMissingStep(
                        PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers,
                        isCheckingAnswers = true,
                        saveAfterSubmit = false,
                    ),
                    gasSafetyCheckYourAnswersStep,
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
            )

    // TODO PRSD-1245, 1247, 1313 - add this as the handleSubmitAndRedirect method
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
