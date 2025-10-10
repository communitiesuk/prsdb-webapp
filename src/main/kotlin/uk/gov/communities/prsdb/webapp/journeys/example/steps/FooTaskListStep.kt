package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStep
import uk.gov.communities.prsdb.webapp.journeys.example.FooJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskStatusViewModel

@Scope("prototype")
@PrsdbWebComponent
class FooTaskListStep : AbstractGenericStep<Complete, NoInputFormModel, FooJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepSpecificContent(state: FooJourneyState): Map<String, Any> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: FooJourneyState): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "propertyCompliance.taskList.upload.heading",
                    "upload-certificates",
                    listOf(
                        TaskListItemViewModel(
                            "OccupationTask",
                            TaskStatusViewModel.fromStatus(TaskStatus.NOT_STARTED),
                            url = state.occupied?.routeSegment,
                        ),
                        TaskListItemViewModel(
                            "EpcTask",
                            TaskStatusViewModel.fromStatus(TaskStatus.NOT_STARTED),
                            url = state.epcQuestion?.routeSegment,
                        ),
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

    override fun chooseTemplate(): String = "taskList"

    override fun mode(state: FooJourneyState): Nothing? = null
}
