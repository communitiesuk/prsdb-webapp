package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.newJourneys.Complete
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.FooJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskStatusViewModel

@Scope("prototype")
@PrsdbWebComponent
class FooTaskListStep : AbstractStep<Complete, NoInputFormModel, FooJourneyState>() {
    override val formModelClazz = NoInputFormModel::class

    override fun getStepContent(state: FooJourneyState): Map<String, Any> = mapOf("taskListViewModel" to getTaskListViewModel())

    fun getTaskListViewModel(): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "propertyCompliance.taskList.upload.heading",
                    "upload-certificates",
                    listOf(
                        TaskListItemViewModel(
                            "OccupationTask",
                            TaskStatusViewModel.fromStatus(TaskStatus.NOT_STARTED),
                            // TODO replace with actual step URL
                            url = "occupied",
                        ),
                        TaskListItemViewModel(
                            "EpcTask",
                            TaskStatusViewModel.fromStatus(TaskStatus.NOT_STARTED),
                            // TODO replace with actual step URL
                            url = "has-epc",
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

    override fun chooseTemplate(state: FooJourneyState): String = "taskList"

    override fun mode(state: FooJourneyState): Nothing? = null
}
