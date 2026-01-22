package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.FooJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel

@JourneyFrameworkComponent
class FooTaskListStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, FooJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FooJourneyState): Map<String, Any> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: FooJourneyState): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "propertyCompliance.taskList.upload.heading",
                    "upload-certificates",
                    listOf(
                        TaskListItemViewModel.fromTask("OccupationTask", state.occupationTask),
                        TaskListItemViewModel.fromTask("OccupationTask", state.epcTask),
                    ),
                ),
            )

        return TaskListViewModel(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf("propertyCompliance.taskList.subtitle.one"),
            sectionViewModels,
        )
    }

    override fun chooseTemplate(state: FooJourneyState): String = "taskList"

    override fun mode(state: FooJourneyState): Nothing? = null
}

@JourneyFrameworkComponent
final class FooTaskListStep(
    stepConfig: FooTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, FooJourneyState>(stepConfig)
