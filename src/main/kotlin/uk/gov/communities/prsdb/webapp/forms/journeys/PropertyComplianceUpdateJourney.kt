package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceUpdateStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
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
) : GroupedUpdateJourney<PropertyComplianceUpdateStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE_UPDATE,
        initialStepId = PropertyComplianceUpdateStepId.UpdateGasSafety,
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

    override val sections: List<JourneySection<PropertyComplianceUpdateStepId>>
        get() =
            createSingleSectionWithSingleTaskFromSteps(
                initialStepId,
                setOf(gasSafetyStep),
            )

    // TODO PRSD-1244: Implement gas safety step
    private val gasSafetyStep =
        Step(
            id = PropertyComplianceUpdateStepId.UpdateGasSafety,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/todo",
                    content =
                        mapOf("todoComment" to "TODO PRSD-1244: Implement gas safety step"),
                ),
        )

    private fun updateComplianceAndRedirect(): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val complianceUpdate = PropertyComplianceUpdateModel()

        propertyComplianceService.updatePropertyCompliance(propertyOwnershipId, complianceUpdate)

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
    }
}
