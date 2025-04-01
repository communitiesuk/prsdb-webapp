package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyComplianceJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : JourneyWithTaskList<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE,
        initialStepId = PropertyComplianceStepId.GasSafety,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val sections =
        listOf(
            JourneySection(uploadTasks, "propertyCompliance.taskList.upload.heading", "upload-certificates"),
            JourneySection(checkAndSubmitTasks, "propertyCompliance.taskList.checkAndSubmit.heading", "check-and-submit"),
        )

    override val taskListFactory =
        getTaskListViewModelFactory(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf("propertyCompliance.taskList.subtitle.one", "propertyCompliance.taskList.subtitle.two"),
            numberSections = false,
        )

    private val uploadTasks
        get() =
            listOf(
                // TODO PRSD-942: Implement gas safety certificate upload task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.GasSafety, "TODO PRSD-942: Implement gas safety certificate upload task"),
                    "propertyCompliance.taskList.upload.gasSafety",
                ),
                // TODO PRSD-954: Implement EICR upload task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.EICR, "TODO PRSD-954: Implement EICR upload task"),
                    "propertyCompliance.taskList.upload.eicr",
                ),
                // TODO PRSD-395: Implement EICR upload task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.EPC, "TODO PRSD-395: Implement EPC task"),
                    "propertyCompliance.taskList.upload.epc",
                    "propertyCompliance.taskList.upload.epc.hint",
                ),
            )

    private val checkAndSubmitTasks
        get() =
            listOf(
                // TODO PRSD-962: Implement check and submit task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.CheckAndSubmit, "TODO PRSD-962: Implement check and submit task"),
                    "propertyCompliance.taskList.checkAndSubmit.check",
                ),
                // TODO PRSD-963: Implement declaration task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.Declaration, "TODO PRSD-963: Implement declaration task"),
                    "propertyCompliance.taskList.checkAndSubmit.declare",
                ),
            )

    private fun placeholderStep(
        stepId: PropertyComplianceStepId,
        todoComment: String,
    ) = Step(
        id = stepId,
        page = Page(formModel = NoInputFormModel::class, templateName = "todo", content = mapOf("todoComment" to todoComment)),
    )
}
