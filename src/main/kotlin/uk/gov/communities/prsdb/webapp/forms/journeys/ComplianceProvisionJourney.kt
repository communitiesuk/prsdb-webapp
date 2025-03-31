package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.ProvideComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class ComplianceProvisionJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : JourneyWithTaskList<ProvideComplianceStepId>(
        journeyType = JourneyType.COMPLIANCE_PROVISION,
        initialStepId = ProvideComplianceStepId.InitialPlaceholder,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val sections =
        listOf(
            JourneySection(uploadTasks, "provideCompliance.taskList.upload.heading", "upload-certificates"),
            JourneySection(checkAndSubmitTasks, "provideCompliance.taskList.checkAndSubmit.heading", "check-and-submit"),
        )

    override val taskListFactory =
        getTaskListViewModelFactory(
            "provideCompliance.title",
            "provideCompliance.taskList.heading",
            listOf("provideCompliance.taskList.subtitle.one", "provideCompliance.taskList.subtitle.two"),
            numberSections = false,
        )

    private val uploadTasks
        get() =
            listOf(
                // TODO PRSD-942: Implement gas safety certificate upload task
                JourneyTask.withOneStep(
                    placeholderStep(isInitialStep = true),
                    "provideCompliance.taskList.upload.gasSafety",
                ),
                // TODO PRSD-954: Implement EICR upload task
                JourneyTask.withOneStep(
                    placeholderStep(),
                    "provideCompliance.taskList.upload.eicr",
                ),
                JourneyTask.withOneStep(
                    placeholderStep(),
                    "provideCompliance.taskList.upload.epc",
                    "provideCompliance.taskList.upload.epc.hint",
                ),
            )

    private val checkAndSubmitTasks
        get() =
            listOf(
                // TODO PRSD-962: Implement check and submit task
                JourneyTask.withOneStep(placeholderStep(), "provideCompliance.taskList.checkAndSubmit.check"),
                // TODO PRSD-963: Implement declaration task
                JourneyTask.withOneStep(placeholderStep(), "provideCompliance.taskList.checkAndSubmit.declare"),
            )

    private fun placeholderStep(isInitialStep: Boolean = false) =
        Step(
            id = if (isInitialStep) ProvideComplianceStepId.InitialPlaceholder else ProvideComplianceStepId.NonInitialPlaceholder,
            page = Page(formModel = NoInputFormModel::class, templateName = "error/404", content = emptyMap()),
        )
}
