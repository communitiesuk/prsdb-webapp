package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel

@JourneyFrameworkComponent
class PropertyComplianceTaskListStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyComplianceJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyComplianceJourneyState): Map<String, Any?> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: PropertyComplianceJourneyState): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "propertyCompliance.taskList.upload.heading",
                    "upload-documents",
                    listOf(
                       /* TaskListItemViewModel.fromTask("propertyCompliance.taskList.upload.gasSafety", state.gasSafetyTask),
                        TaskListItemViewModel.fromTask("propertyCompliance.taskList.upload.eicr", state.eicrTask),
                        TaskListItemViewModel.fromTask(
                            "propertyCompliance.taskList.upload.epc",
                            state.epcTask,
                            "propertyCompliance.taskList.upload.epc.hint",
                        ),*/
                    ),
                ),
            )

        return TaskListViewModel(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf(
                "propertyCompliance.taskList.subtitle.one",
                "propertyCompliance.taskList.subtitle.two",
                "propertyCompliance.taskList.subtitle.three",
            ),
            sectionViewModels,
        )
    }

    override fun chooseTemplate(state: PropertyComplianceJourneyState): String = "taskList"

    override fun mode(state: PropertyComplianceJourneyState): Nothing? = null
}

@JourneyFrameworkComponent
final class PropertyComplianceTaskListStep(
    stepConfig: PropertyComplianceTaskListStepConfig,
) : JourneyStep.RequestableStep<Complete, NoInputFormModel, PropertyComplianceJourneyState>(stepConfig)
